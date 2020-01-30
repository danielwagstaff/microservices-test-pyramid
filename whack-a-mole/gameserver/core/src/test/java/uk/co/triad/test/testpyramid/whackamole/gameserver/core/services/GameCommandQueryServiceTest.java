package uk.co.triad.test.testpyramid.whackamole.gameserver.core.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GameCommandQueryServiceTest
{
  @Mock private Game mockedGame;
  @InjectMocks private GameCommandQueryService gameCommandQueryService;
  private static final String EXCEPTION_EXPECTED = "Exception should have been thrown";

  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    gameCommandQueryService = new GameCommandQueryService(mockedGame);
  }

  @Test
  public void shouldRegisterPlayerAndCreateOneEvent() throws ExecutionException, InterruptedException
  {
    when(mockedGame.register(any(Player.class))).thenReturn(CompletableFuture.completedFuture(null));

    gameCommandQueryService.register(Player.newPlayer("dan")).toCompletableFuture().get();

    verify(mockedGame, times(1)).register(any(Player.class));
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldThrowExceptionAndCreateOneEventForPlayerDoubleRegistration() throws
                                                                                 InterruptedException,
                                                                                 ExecutionException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally((new PlayerAlreadyRegisteredException("TestDuplicatedPlayer")));
    when(mockedGame.register(any(Player.class))).thenReturn(failFlow);

    try
    {
      gameCommandQueryService.register(Player.newPlayer("dan")).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerAlreadyRegisteredException.class, e.getCause().getClass());
    }

    verify(mockedGame, times(1)).register(any(Player.class));
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldRemovePlayerAndCreateOneEvent() throws ExecutionException, InterruptedException
  {
    when(mockedGame.remove(any(Player.class))).thenReturn(CompletableFuture.completedFuture(null));

    gameCommandQueryService.remove(Player.newPlayer("dan")).toCompletableFuture().get();

    verify(mockedGame, times(1)).remove(any(Player.class));
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldThrowExceptionAndCreateOneEventForNonPlayerRemoval() throws ExecutionException, InterruptedException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally((new PlayerNotRegisteredException("TestNonRegisteredPlayer")));
    when(mockedGame.remove(any(Player.class))).thenReturn(failFlow);

    try
    {
      gameCommandQueryService.remove(Player.newPlayer("dan")).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerNotRegisteredException.class, e.getCause().getClass());
    }

    verify(mockedGame, times(1)).remove(any(Player.class));
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldReturnActivePlayers() throws ExecutionException, InterruptedException
  {
    when(mockedGame.activePlayers()).thenReturn(CompletableFuture.completedFuture(List.of(Player.newPlayer(""))));

    gameCommandQueryService.activePlayers().toCompletableFuture().get();

    verify(mockedGame, times(1)).activePlayers();
  }

  @Test
  public void shouldStartGameAndCreateOneEvent() throws ExecutionException, InterruptedException
  {
    when(mockedGame.start()).thenReturn(CompletableFuture.completedFuture(null));

    gameCommandQueryService.start().toCompletableFuture().get();

    verify(mockedGame, times(1)).start();
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldStopGameAndCreateOneEvent() throws ExecutionException, InterruptedException
  {
    when(mockedGame.stop()).thenReturn(CompletableFuture.completedFuture(null));

    gameCommandQueryService.stop().toCompletableFuture().get();

    verify(mockedGame, times(1)).stop();
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldReturnActiveMoles() throws ExecutionException, InterruptedException
  {
    when(mockedGame.activeMoles()).thenReturn(CompletableFuture.completedFuture(List.of(Mole.newMole())));

    gameCommandQueryService.activeMoles().toCompletableFuture().get();

    verify(mockedGame, times(1)).activeMoles();
  }

  @Test
  public void shouldReturnTrueForWhackedMoleAndCreateOneEvent() throws ExecutionException, InterruptedException
  {
    when(mockedGame.whack(any(Mole.class),
                          any(Player.class))).thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

    var result = gameCommandQueryService.whack(Mole.newMole(), Player.newPlayer("")).toCompletableFuture().get();

    verify(mockedGame, times(1)).whack(any(Mole.class), any(Player.class));
    assertEquals(Boolean.TRUE, result);
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldReturnFalseForMissedMoleAndCreateOneEvent() throws ExecutionException, InterruptedException
  {
    when(mockedGame.whack(any(Mole.class),
                          any(Player.class))).thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));

    var result = gameCommandQueryService.whack(Mole.newMole(), Player.newPlayer("")).toCompletableFuture().get();

    verify(mockedGame, times(1)).whack(any(Mole.class), any(Player.class));
    assertEquals(Boolean.FALSE, result);
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }

  @Test
  public void shouldThrownExceptionForNonPlayerWhackAttemptAndCreateOneEvent() throws
                                                                               ExecutionException,
                                                                               InterruptedException
  {
    var failFlow = new CompletableFuture<Boolean>();
    failFlow.completeExceptionally((new PlayerNotRegisteredException("TestNonRegisteredPlayer")));
    when(mockedGame.whack(any(Mole.class), any(Player.class))).thenReturn(failFlow);

    try
    {
      gameCommandQueryService.whack(Mole.newMole(), Player.newPlayer("")).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerNotRegisteredException.class, e.getCause().getClass());
    }

    verify(mockedGame, times(1)).whack(any(Mole.class), any(Player.class));
    assertEquals(1, gameCommandQueryService.getUnreadEvents().toCompletableFuture().get().size());
  }
}
