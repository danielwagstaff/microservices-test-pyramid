package uk.co.triad.test.testpyramid.whackamole.gameclient.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameclient.services.GameService;
import uk.co.triad.test.testpyramid.whackamole.gameclient.services.PlayerCommandQueryService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlayerCommandQueryServiceTest
{
  @Mock private Player mockedPlayer;
  @Mock private GameService mockedGameService;
  @InjectMocks private PlayerCommandQueryService playerCommandQueryService;

  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    playerCommandQueryService = new PlayerCommandQueryService(mockedPlayer, mockedGameService);
  }

  @Test
  public void shouldRegisterPlayerAndStartPlaying() throws ExecutionException, InterruptedException
  {
    when(mockedPlayer.joinGame(any(GameService.class))).thenReturn(CompletableFuture.completedFuture(null));

    playerCommandQueryService.startPlaying().toCompletableFuture().get();

    verify(mockedPlayer, times(1)).autoplay(any(GameService.class));
  }

  @Test
  public void shouldNotPlayIfRegistrationFails() throws InterruptedException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally((new Exception("Registration Failed")));
    when(mockedPlayer.joinGame(any(GameService.class))).thenReturn(failFlow);

    try
    {
      playerCommandQueryService.startPlaying().toCompletableFuture().get();
      fail("Exception should have been thrown");
    }
    catch (ExecutionException e)
    {
      assertEquals(Exception.class, e.getCause().getClass());
    }
    verify(mockedPlayer, never()).autoplay(any(GameService.class));
  }

  @Test
  public void shouldStopPlayingAndLeaveGame() throws ExecutionException, InterruptedException
  {
    when(mockedPlayer.leaveGame(any(GameService.class))).thenReturn(CompletableFuture.completedFuture(null));

    playerCommandQueryService.stopPlaying().toCompletableFuture().get();

    verify(mockedPlayer, times(1)).leaveGame(any(GameService.class));
  }

  @Test
  public void shouldRethrowExceptionIfLeavingFails() throws InterruptedException
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally((new Exception("Registration Failed")));
    when(mockedPlayer.leaveGame(any(GameService.class))).thenReturn(failFlow);

    try
    {
      playerCommandQueryService.stopPlaying().toCompletableFuture().get();
      fail("Exception should have been thrown");
    }
    catch (ExecutionException e)
    {
      assertEquals(Exception.class, e.getCause().getClass());
    }
  }
}
