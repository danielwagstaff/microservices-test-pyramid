package uk.co.triad.test.testpyramid.whackamole.gameserver.core.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class GameCommandQueryService
{
  private Game game;
  private ConcurrentLinkedQueue<String> events = new ConcurrentLinkedQueue<>();

  @Autowired
  public GameCommandQueryService(Game game)
  {
    this.game = game;
  }

  public CompletionStage<List<String>> getUnreadEvents()
  {
    var currentEvents = new ArrayList<String>();

    var event = events.poll();
    while (event != null)
    {
      currentEvents.add(event);
      event = events.poll();
    }

    return CompletableFuture.supplyAsync(() -> currentEvents);
  }

  public CompletionStage<Void> register(Player player)
  {
    return game.register(player).thenRun(() -> events.add(player.getName() + " joined the game")).exceptionally(t -> {
      if (t.getCause() instanceof PlayerAlreadyRegisteredException)
      {
        events.add(player.getName() + " tried to join the game again!");
      }
      throw new CompletionException(t.getCause());
    });
  }

  public CompletionStage<Void> remove(Player player)
  {
    return game.remove(player).thenRun(() -> events.add(player.getName() + " left the game")).exceptionally(t -> {
      if (t.getCause() instanceof PlayerNotRegisteredException)
      {
        events.add(player.getName() + " tried to leave the game, despite having not joined yet!");
      }
      throw new CompletionException(t.getCause());
    });
  }

  public CompletionStage<List<Player>> activePlayers()
  {
    return game.activePlayers();
  }

  public CompletionStage<Void> start()
  {
    return game.start().thenRun(() -> events.add("Game started!"));
  }

  public CompletionStage<Void> stop()
  {
    return game.stop().thenRun(() -> events.add("Game stopped!"));
  }

  public CompletionStage<List<Mole>> activeMoles()
  {
    return game.activeMoles();
  }

  public CompletionStage<Boolean> whack(Mole mole, Player player)
  {
    return game.whack(mole, player).thenApply(result -> {
      if (Boolean.TRUE.equals(result))
      {
        events.add(player.getName() + " hit mole " + mole.getId());
      }
      else
      {
        events.add(player.getName() + " missed mole " + mole.getId());
      }
      return result;
    }).exceptionally(t -> {
      if (t.getCause() instanceof PlayerNotRegisteredException)
      {
        events.add(player.getName() + " cannot hit moles, as they are not registered");
      }
      throw new CompletionException(t.getCause());
    });
  }
}
