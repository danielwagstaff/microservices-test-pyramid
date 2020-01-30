package uk.co.triad.test.testpyramid.whackamole.gameclient.domain;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.triad.test.testpyramid.whackamole.gameclient.services.GameService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlayerTest
{
  private static final String MOCK_PLAYER_NAME = "dan";
  private Player player;
  @Mock private GameService mockedGameService;

  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    player = new Player(MOCK_PLAYER_NAME);
  }

  @Test
  public void shouldJoinGame() throws ExecutionException, InterruptedException
  {
    when(mockedGameService.register(any(String.class))).thenReturn(CompletableFuture.completedFuture(null));

    player.joinGame(mockedGameService).toCompletableFuture().get();

    verify(mockedGameService, times(1)).register(any(String.class));
  }

  @Test
  public void shouldNotAttemptToJoinGameTwice() throws ExecutionException, InterruptedException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new PlayerAlreadyRegisteredException("TestAlreadyExistentPlayer"));
    when(mockedGameService.register(any(String.class))).thenReturn(CompletableFuture.completedFuture(null))
                                                       .thenReturn(failFlow);

    player.joinGame(mockedGameService).toCompletableFuture().get();
    player.joinGame(mockedGameService).toCompletableFuture().get();

    verify(mockedGameService, times(1)).register(any(String.class));
  }

  @Test
  public void shouldCompleteExceptionallyForFailedJoin() throws InterruptedException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new Exception("TestAlreadyExistentPlayer"));
    when(mockedGameService.register(any(String.class))).thenReturn(failFlow);

    try
    {
      player.joinGame(mockedGameService).toCompletableFuture().get();
      fail("Exception should have been thrown");
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerJoinGameFailedException.class, e.getCause().getClass());
    }
    verify(mockedGameService, times(1)).register(any(String.class));
  }

  @Test
  public void shouldLeaveGame() throws ExecutionException, InterruptedException
  {
    when(mockedGameService.deregister(any(String.class))).thenReturn(CompletableFuture.completedFuture(null));

    player.leaveGame(mockedGameService).toCompletableFuture().get();

    verify(mockedGameService, times(1)).deregister(any(String.class));
  }

  @Test
  public void shouldNotLeaveGameIfNotJoined() throws ExecutionException, InterruptedException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new PlayerNotRegisteredException("TestNonExistentPlayer"));
    when(mockedGameService.deregister(any(String.class))).thenReturn(failFlow);

    player.leaveGame(mockedGameService).toCompletableFuture().get();

    verify(mockedGameService, times(1)).deregister(any(String.class));
  }

  @Test
  public void shouldCompleteExceptionallyForFailedLeave() throws InterruptedException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new Exception("TestNonExistentPlayer"));
    when(mockedGameService.deregister(any(String.class))).thenReturn(failFlow);

    try
    {
      player.leaveGame(mockedGameService).toCompletableFuture().get();
      fail("Exception should have been thrown");
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerLeaveGameFailedException.class, e.getCause().getClass());
    }
    verify(mockedGameService, times(1)).deregister(any(String.class));
  }

  @Test
  public void shouldGetAllActiveMolesAndWhackThemAll() throws ExecutionException, InterruptedException
  {
    var mole1 = new Mole(UUID.randomUUID());
    var mole2 = new Mole(UUID.randomUUID());
    var mole3 = new Mole(UUID.randomUUID());
    var mockedActiveMoles = List.of(mole1, mole2, mole3);

    when(mockedGameService.register(any(String.class))).thenReturn(CompletableFuture.completedFuture(null));
    when(mockedGameService.activeMoles()).thenReturn(CompletableFuture.completedFuture(mockedActiveMoles))
                                         .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));
    when(mockedGameService.whack(any(String.class), any(Mole.class))).thenReturn(CompletableFuture.completedFuture(
        Boolean.TRUE));

    player.joinGame(mockedGameService).toCompletableFuture().get();
    player.autoplay(mockedGameService).toCompletableFuture().get();

    verify(mockedGameService, timeout(2000L).times(1)).whack(any(String.class), eq(mole1));
    verify(mockedGameService, timeout(2000L).times(1)).whack(any(String.class), eq(mole2));
    verify(mockedGameService, timeout(2000L).times(1)).whack(any(String.class), eq(mole3));
  }

  @Test
  public void shouldNotBeAbleToWhackMoleWhenNotRegistered() throws ExecutionException, InterruptedException
  {
    var mole1 = new Mole(UUID.randomUUID());
    var mole2 = new Mole(UUID.randomUUID());
    var mole3 = new Mole(UUID.randomUUID());
    var mockedActiveMoles = List.of(mole1, mole2, mole3);

    when(mockedGameService.register(any(String.class))).thenReturn(CompletableFuture.completedFuture(null));
    when(mockedGameService.activeMoles()).thenReturn(CompletableFuture.completedFuture(mockedActiveMoles))
                                         .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));
    var failFlow = new CompletableFuture<Boolean>();
    failFlow.completeExceptionally(new PlayerNotRegisteredException("TestNonExistentPlayer"));
    when(mockedGameService.whack(any(String.class), any(Mole.class))).thenReturn(failFlow);

    player.joinGame(mockedGameService).toCompletableFuture().get();
    player.autoplay(mockedGameService).toCompletableFuture().get();

    verify(mockedGameService, timeout(2000L).times(1)).whack(any(String.class), eq(mole1));
    verify(mockedGameService, timeout(2000L).times(1)).whack(any(String.class), eq(mole2));
    verify(mockedGameService, timeout(2000L).times(1)).whack(any(String.class), eq(mole3));
  }

  @Test
  public void shouldNotAttemptToHitMolesWhenFailedToGetMoles() throws ExecutionException, InterruptedException
  {
    var failFlow = new CompletableFuture<List<Mole>>();
    failFlow.completeExceptionally(new Exception("Failed to get moles"));
    when(mockedGameService.activeMoles()).thenReturn(failFlow);

    when(mockedGameService.register(any(String.class))).thenReturn(CompletableFuture.completedFuture(null));
    when(mockedGameService.activeMoles()).thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

    player.joinGame(mockedGameService).toCompletableFuture().get();
    player.autoplay(mockedGameService).toCompletableFuture().get();

    verify(mockedGameService, times(0)).whack(any(String.class), any(Mole.class));
  }
}
