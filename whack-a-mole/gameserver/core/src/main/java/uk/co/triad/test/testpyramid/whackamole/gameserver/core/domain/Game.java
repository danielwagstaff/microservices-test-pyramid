package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class Game
{
  private static final int WHACK_POINTS = 10;
  private final Logger logger = LoggerFactory.getLogger(Game.class);
  private final List<Player> activePlayers = new ArrayList<>();
  private final List<Player> syncdActivePlayers = Collections.synchronizedList(activePlayers);
  private final List<Mole> activeMoles = new ArrayList<>();
  private final List<Mole> syncdActiveMoles = Collections.synchronizedList(activeMoles);
  private Timer gameStepTimer;

  public CompletionStage<Void> register(Player player)
  {
    return CompletableFuture.runAsync(() -> {
      if (!syncdActivePlayers.contains(player))
      {
        syncdActivePlayers.add(player);
      }
      else
      {
        throw new CompletionException(new PlayerAlreadyRegisteredException("Player already registered: " + player.getName()));
      }
    });
  }

  public CompletionStage<Void> remove(Player player)
  {
    return CompletableFuture.runAsync(() -> {
      if (syncdActivePlayers.contains(player))
      {
        syncdActivePlayers.remove(player);
      }
      else
      {
        throw new CompletionException(new PlayerNotRegisteredException("Player not registered: " + player.getName()));
      }
    });
  }

  public CompletionStage<List<Player>> activePlayers()
  {
    return CompletableFuture.supplyAsync(() -> new ArrayList<>(syncdActivePlayers));
  }

  private void clearAllScores()
  {
    synchronized (syncdActivePlayers)
    {
      syncdActivePlayers.forEach(player -> player.getScore().setTotal(0));
    }
  }

  public CompletionStage<Void> start()
  {
    return CompletableFuture.runAsync(() -> {
      clearAllScores();
      if (gameStepTimer == null)
      {
        gameStepTimer = new Timer();
        gameStepTimer.schedule(new TimerTask()
        {
          @Override
          public void run()
          {
            try
            {
              Thread.sleep(ThreadLocalRandom.current().nextLong(500));
              syncdActiveMoles.add(Mole.newMole());
            }
            catch (InterruptedException e)
            {
              logger.error("Thread interrupted: {}", e.getCause().getMessage());
              gameStepTimer.cancel();
              gameStepTimer = null;
              Thread.currentThread().interrupt();
            }
          }
        }, 0L, 500L);
      }
    });
  }

  public CompletionStage<Void> stop()
  {
    return CompletableFuture.runAsync(() -> {
      if (gameStepTimer != null)
      {
        gameStepTimer.cancel();
        gameStepTimer = null;
      }
      syncdActiveMoles.clear();
    });
  }

  public CompletionStage<Boolean> whack(Mole mole, Player player)
  {
    return CompletableFuture.supplyAsync(() -> {
      if (syncdActivePlayers.contains(player))
      {
        synchronized (syncdActiveMoles)
        {
          if (syncdActiveMoles.contains(mole))
          {
            synchronized (syncdActivePlayers)
            {
              syncdActivePlayers.get(syncdActivePlayers.indexOf(player)).getScore().increment(WHACK_POINTS);
            }
            syncdActiveMoles.remove(mole);
            return true;
          }
          else
          {
            return false;
          }
        }
      }
      else
      {
        throw new CompletionException(new PlayerNotRegisteredException("Player not registered: " + player.getName()));
      }
    });
  }

  public CompletionStage<List<Mole>> activeMoles()
  {
    return CompletableFuture.supplyAsync(() -> new ArrayList<>(syncdActiveMoles));
  }
}
