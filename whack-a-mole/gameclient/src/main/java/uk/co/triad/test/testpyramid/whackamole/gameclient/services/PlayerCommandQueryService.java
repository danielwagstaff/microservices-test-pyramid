package uk.co.triad.test.testpyramid.whackamole.gameclient.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.Player;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Service
public class PlayerCommandQueryService
{
  private static final Logger LOGGER = LoggerFactory.getLogger(PlayerCommandQueryService.class);

  private Player player;
  private GameService gameService;

  @Autowired
  public PlayerCommandQueryService(Player player, GameService gameService)
  {
    this.player = player;
    this.gameService = gameService;
  }

  public CompletionStage<Void> startPlaying()
  {
    return player.joinGame(gameService).exceptionally(t -> {
      LOGGER.error("Could not join game {}", t.getMessage());
      throw new CompletionException(t);
    }).thenRun(() -> player.autoplay(gameService));
  }

  public CompletionStage<Void> stopPlaying()
  {
    return player.leaveGame(gameService).exceptionally(t -> {
      LOGGER.error("Could not leave game {}", t.getMessage());
      throw new CompletionException(t);
    });
  }
}
