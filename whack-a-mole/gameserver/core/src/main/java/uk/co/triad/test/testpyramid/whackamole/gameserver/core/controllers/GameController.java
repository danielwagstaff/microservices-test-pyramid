package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.EventDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.MoleIdDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerScoreDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.WhackDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers.EventDtoMapper;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers.MoleIdDtoMapper;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers.PlayerScoreDtoMapper;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers.WhackDtoMapper;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Mole;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerNotRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.services.GameCommandQueryService;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@RestController
@RequestMapping("game")
public class GameController
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);
  private GameCommandQueryService gameCommandQueryService;

  @Autowired
  public GameController(GameCommandQueryService gameCommandQueryService)
  {
    this.gameCommandQueryService = gameCommandQueryService;
  }

  @PostMapping(path = "start")
  public CompletionStage<ResponseEntity<Void>> start()
  {
    LOGGER.info("Start game");
    return gameCommandQueryService.start().thenApply(ResponseEntity::ok);
  }

  @PostMapping(path = "stop")
  public CompletionStage<ResponseEntity<Void>> stop()
  {
    LOGGER.info("Stop game");
    return gameCommandQueryService.stop().thenApply(ResponseEntity::ok);
  }

  @GetMapping(path = "moles")
  public CompletionStage<ResponseEntity<List<MoleIdDto>>> listMoles()
  {
    LOGGER.info("View all active moles");
    return gameCommandQueryService.activeMoles()
                                  .thenApply(moles -> ResponseEntity.ok(moles.stream()
                                                                             .map(MoleIdDtoMapper::createDto)
                                                                             .collect(Collectors.toList())));
  }

  @PostMapping(path = "moles")
  public CompletionStage<ResponseEntity<Void>> whack(@RequestBody WhackDto whackDto)
  {
    LOGGER.info("Whack a mole");
    return gameCommandQueryService.whack(Mole.newMole(WhackDtoMapper.createMoleId(whackDto)),
                                         Player.newPlayer(WhackDtoMapper.createPlayerName(whackDto)))
                                  .thenApply(result -> {
                                    if (Boolean.TRUE.equals(result))
                                    {
                                      return new ResponseEntity<Void>(HttpStatus.OK);
                                    }
                                    else
                                    {
                                      return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
                                    }
                                  })
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

  @GetMapping(path = "events")
  public CompletionStage<ResponseEntity<List<EventDto>>> unreadEvents()
  {
    LOGGER.info("Get unread events");
    return gameCommandQueryService.getUnreadEvents()
                                  .thenApply(events -> ResponseEntity.ok(events.stream()
                                                                               .map(EventDtoMapper::createDto)
                                                                               .collect(Collectors.toList())));
  }

  @GetMapping(path = "scores")
  public CompletionStage<ResponseEntity<List<PlayerScoreDto>>> listScores()
  {
    LOGGER.info("Get scores for all players");
    return gameCommandQueryService.activePlayers()
                                  .thenApply(players -> ResponseEntity.ok(players.stream()
                                                                                 .map(PlayerScoreDtoMapper::createDto)
                                                                                 .collect(Collectors.toList())));
  }
}
