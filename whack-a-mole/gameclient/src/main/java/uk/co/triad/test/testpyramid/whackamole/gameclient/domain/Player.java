package uk.co.triad.test.testpyramid.whackamole.gameclient.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.triad.test.testpyramid.whackamole.gameclient.services.GameService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Player
{
  private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);
  private final String playerName;
  private final AtomicBoolean isRegistered;
  private Timer gameStepTimer;

  public Player(@Value("${player.name}") final String playerName)
  {
    this.playerName = playerName;
    this.isRegistered = new AtomicBoolean(false);
  }

  public CompletionStage<Void> joinGame(GameService gameService)
  {
    if (!isRegistered.get())
    {
      return gameService.register(playerName).exceptionally(t -> {
        if (t instanceof PlayerAlreadyRegisteredException)
        {
          LOGGER.info("Name already registered in game - could be by me, or could be someone else!");
          isRegistered.set(true);
          return null;
        }
        else
        {
          LOGGER.info("Game registration failed: {}", t.getMessage());
          throw new CompletionException(new PlayerJoinGameFailedException(t));
        }
      }).thenAccept(res -> {
        LOGGER.info("Successfully joined the game");
        isRegistered.set(true);
      });
    }
    else
    {
      return CompletableFuture.runAsync(() -> LOGGER.info("I'm already registered!"));
    }
  }

  public CompletionStage<Void> leaveGame(GameService gameService)
  {
    return this.pause().thenCompose(voidArg -> gameService.deregister(playerName).exceptionally(t -> {
      if (t instanceof PlayerNotRegisteredException)
      {
        LOGGER.info("Player not registered");
        isRegistered.set(false);
        return null;
      }
      else
      {
        LOGGER.info("Could not leave the game: {}", t.getMessage());
        throw new CompletionException(new PlayerLeaveGameFailedException(t));
      }
    }).thenAccept(res -> {
      LOGGER.info("Successfully left the game");
      isRegistered.set(false);
    }));
  }

  private void whackMole(GameService gameService, String playerName, Mole mole)
  {
    gameService.whack(playerName, mole).exceptionally(t -> {
      LOGGER.info("Whacking mole {}", mole.getId());
      if (t instanceof PlayerNotRegisteredException)
      {
        LOGGER.error("Could not whack mole, as player is not registered");
      }
      else
      {
        LOGGER.info("Could not whack mole: {}", t.getMessage());
      }
      return null;
    }).thenAccept(res -> {
      if (Boolean.TRUE.equals(res))
      {
        LOGGER.info("Successfully hit mole");
      }
      else
      {
        LOGGER.info("Failed to hit mole");
      }
    });
  }

  public CompletionStage<Void> autoplay(GameService gameService)
  {
    return CompletableFuture.runAsync(() -> {
      if (gameStepTimer == null && isRegistered.get())
      {
        gameStepTimer = new Timer();
        gameStepTimer.schedule(new TimerTask()
        {
          @Override
          public void run()
          {
            gameService.activeMoles().exceptionally(t -> {
              LOGGER.info("Failed to get moles: {}", t.getMessage());
              return null;
            }).thenAccept(moles -> moles.forEach(mole -> whackMole(gameService, playerName, mole)));
          }
        }, 0L, 500L);
      }
    });
  }

  public CompletionStage<Void> pause()
  {
    return CompletableFuture.runAsync(() -> {
      if (gameStepTimer != null)
      {
        gameStepTimer.cancel();
        gameStepTimer = null;
      }
    });
  }
}
