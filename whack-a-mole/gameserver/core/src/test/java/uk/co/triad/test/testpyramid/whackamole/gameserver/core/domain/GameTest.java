package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class GameTest
{
  private final static String MOCK_PLAYER_NAME = "dan";
  private static final String EXCEPTION_EXPECTED = "Exception should have been thrown";
  private Game game;

  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    game = new Game();
  }

  @Test
  public void shouldAllowRegistrationOfUniquePlayer() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);

    game.register(mockPlayer).toCompletableFuture().get();

    assertTrue(game.activePlayers().toCompletableFuture().get().contains(mockPlayer));
  }

  @Test
  public void shouldThrowExceptionForRegistrationOfNonUniquePlayer() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);

    game.register(mockPlayer).toCompletableFuture().get();

    try
    {
      game.register(mockPlayer).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerAlreadyRegisteredException.class, e.getCause().getClass());
    }
  }

  @Test
  public void shouldAllowRemovalOfRegisteredPlayer() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);

    game.register(mockPlayer).toCompletableFuture().get();
    assertTrue(game.activePlayers().toCompletableFuture().get().contains(mockPlayer));

    game.remove(mockPlayer).toCompletableFuture().get();
    assertFalse(game.activePlayers().toCompletableFuture().get().contains(mockPlayer));
  }

  @Test
  public void shouldThrowExceptionForRemovalOfNonRegisteredPlayer() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);

    assertFalse(game.activePlayers().toCompletableFuture().get().contains(mockPlayer));

    try
    {
      game.remove(mockPlayer).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerNotRegisteredException.class, e.getCause().getClass());
    }
  }

  @Test
  public void shouldNotRememberScoreForRemovedPlayer() throws ExecutionException, InterruptedException
  {
    var mockPlayer1 = Player.newPlayer(MOCK_PLAYER_NAME);
    mockPlayer1.getScore().setTotal(10);

    game.register(mockPlayer1).toCompletableFuture().get();

    assertTrue(game.activePlayers().toCompletableFuture().get().contains(mockPlayer1));
    assertEquals(10, game.activePlayers().toCompletableFuture().get().get(0).getScore().getTotal());

    game.remove(mockPlayer1).toCompletableFuture().get();
    assertFalse(game.activePlayers().toCompletableFuture().get().contains(mockPlayer1));

    var mockPlayer2 = Player.newPlayer(MOCK_PLAYER_NAME);
    game.register(mockPlayer2).toCompletableFuture().get();

    assertTrue(game.activePlayers().toCompletableFuture().get().contains(mockPlayer2));
    assertEquals(1, game.activePlayers().toCompletableFuture().get().size());
    assertEquals(0, game.activePlayers().toCompletableFuture().get().get(0).getScore().getTotal());
  }

  @Test
  public void shouldReturnListOfActivePlayers() throws ExecutionException, InterruptedException
  {
    var mockPlayer1 = Player.newPlayer(MOCK_PLAYER_NAME);
    var mockPlayer2 = Player.newPlayer(MOCK_PLAYER_NAME + "1");

    game.register(mockPlayer1).toCompletableFuture().get();
    game.register(mockPlayer2).toCompletableFuture().get();

    var activePlayers = game.activePlayers().toCompletableFuture().get();

    assertEquals(2, activePlayers.size());
    assertTrue(activePlayers.contains(mockPlayer1));
    assertTrue(activePlayers.contains(mockPlayer2));
  }

  @Test
  public void shouldStartAddingMolesToActiveListWhenGameStarted() throws ExecutionException, InterruptedException
  {
    assertEquals(0, game.activeMoles().toCompletableFuture().get().size());

    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());
    assertFalse(game.activeMoles().toCompletableFuture().get().isEmpty());
  }

  @Test
  public void shouldStopAddingAndRemoveAllMolesFromActiveListWhenGameStopped() throws
                                                                               ExecutionException,
                                                                               InterruptedException
  {
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    game.stop().toCompletableFuture().get();

    assertEquals(0, game.activeMoles().toCompletableFuture().get().size());
  }

  @Test
  public void shouldReturnListOfActiveMoles() throws ExecutionException, InterruptedException
  {
    game.start().toCompletableFuture().get();
    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    assertEquals(Mole.class, game.activeMoles().toCompletableFuture().get().get(0).getClass());
  }

  @Test
  public void shouldReturnTrueWhenMoleInActiveListWhacked() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);
    game.register(mockPlayer).toCompletableFuture().get();
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    var doomedMole = game.activeMoles().toCompletableFuture().get().get(0);

    assertTrue(game.whack(doomedMole, mockPlayer).toCompletableFuture().get());
  }

  @Test
  public void shouldReturnFalseWhenWhackAttemptedOnMoleNotInActiveList() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);
    game.register(mockPlayer).toCompletableFuture().get();
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    var moleNotInTheGame = Mole.newMole();

    assertFalse(game.activeMoles().toCompletableFuture().get().contains(moleNotInTheGame));
    assertFalse(game.whack(moleNotInTheGame, mockPlayer).toCompletableFuture().get());
  }

  @Test
  public void shouldRemoveWhackedMolesFromActiveMolesList() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);
    game.register(mockPlayer).toCompletableFuture().get();
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    var doomedMole = game.activeMoles().toCompletableFuture().get().get(0);

    game.whack(doomedMole, mockPlayer).toCompletableFuture().get();

    assertFalse(game.activeMoles().toCompletableFuture().get().contains(doomedMole));
  }

  @Test
  public void shouldThrowExceptionWhenNonPlayerAttemptsToWhackMole() throws ExecutionException, InterruptedException
  {
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    var doomedMole = game.activeMoles().toCompletableFuture().get().get(0);

    try
    {
      game.whack(doomedMole, Player.newPlayer("nonRegisteredPlayer")).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerNotRegisteredException.class, e.getCause().getClass());
    }
  }

  @Test
  public void shouldAddPointsToPlayerWhenMoleWhacked() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);
    game.register(mockPlayer).toCompletableFuture().get();
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    var doomedMole = game.activeMoles().toCompletableFuture().get().get(0);

    game.whack(doomedMole, mockPlayer).toCompletableFuture().get();

    assertTrue(mockPlayer.getScore().getTotal() > 0);
  }

  @Test
  public void shouldNotClearPointsWhenGameStopped() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);
    game.register(mockPlayer).toCompletableFuture().get();
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    var doomedMole = game.activeMoles().toCompletableFuture().get().get(0);

    game.whack(doomedMole, mockPlayer).toCompletableFuture().get();

    assertTrue(mockPlayer.getScore().getTotal() > 0);

    game.stop().toCompletableFuture().get();

    assertTrue(mockPlayer.getScore().getTotal() > 0);
  }

  @Test
  public void shouldClearPointsWhenGameRestarted() throws ExecutionException, InterruptedException
  {
    var mockPlayer = Player.newPlayer(MOCK_PLAYER_NAME);
    game.register(mockPlayer).toCompletableFuture().get();
    game.start().toCompletableFuture().get();

    await().atMost(2, SECONDS).until(() -> !game.activeMoles().toCompletableFuture().get().isEmpty());

    var doomedMole = game.activeMoles().toCompletableFuture().get().get(0);

    game.whack(doomedMole, mockPlayer).toCompletableFuture().get();

    assertTrue(mockPlayer.getScore().getTotal() > 0);

    game.stop().toCompletableFuture().get();
    game.start().toCompletableFuture().get();

    assertEquals(0, mockPlayer.getScore().getTotal());
  }
}
