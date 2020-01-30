package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerNameDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers.PlayerNameDtoMapper;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerAlreadyRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerNotRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.services.GameCommandQueryService;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "players", produces = APPLICATION_JSON_VALUE)
public class RegistrationController
{
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);
  private GameCommandQueryService gameCommandQueryService;

  @Autowired
  public RegistrationController(GameCommandQueryService gameCommandQueryService)
  {
    this.gameCommandQueryService = gameCommandQueryService;
  }

  @PostMapping
  public CompletionStage<ResponseEntity<Void>> register(@RequestBody PlayerNameDto playerNameDto)
  {
    LOGGER.info("Registering player {}", playerNameDto.getPlayerName());
    return gameCommandQueryService.register(Player.newPlayer(playerNameDto.getPlayerName()))
                                  .thenApply(ResponseEntity::ok)
                                  .exceptionally(t -> {
                                    if (t.getCause() instanceof PlayerAlreadyRegisteredException)
                                    {
                                      return new ResponseEntity<>(HttpStatus.CONFLICT);
                                    }
                                    else
                                    {
                                      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                                    }
                                  });
  }

  @DeleteMapping
  public CompletionStage<ResponseEntity<Void>> remove(@RequestBody PlayerNameDto playerNameDto)
  {
    LOGGER.info("Removing player {}", playerNameDto.getPlayerName());
    return gameCommandQueryService.remove(Player.newPlayer(playerNameDto.getPlayerName()))
                                  .thenApply(ResponseEntity::ok)
                                  .exceptionally(t -> {
                                    if (t.getCause() instanceof PlayerNotRegisteredException)
                                    {
                                      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                                    }
                                    else
                                    {
                                      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                                    }
                                  });
  }

  @GetMapping
  public CompletionStage<ResponseEntity<List<PlayerNameDto>>> listPlayers()
  {
    LOGGER.info("View all players");
    return gameCommandQueryService.activePlayers()
                                  .thenApply(players -> ResponseEntity.ok(players.stream()
                                                                                 .map(PlayerNameDtoMapper::createDto)
                                                                                 .collect(Collectors.toList())));
  }
}
