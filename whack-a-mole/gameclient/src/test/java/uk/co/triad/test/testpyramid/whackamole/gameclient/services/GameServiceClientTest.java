package uk.co.triad.test.testpyramid.whackamole.gameclient.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.Mole;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.PlayerAlreadyRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.PlayerNotRegisteredException;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(ids = { "uk.co.triad.test.test-pyramid.whack-a-mole:game-server:1.0-SNAPSHOT:stubs" }, stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class GameServiceClientTest
{
  private static final String PLAYER_NAME_EXISTENT = "existentPlayer";
  private static final String PLAYER_NAME_NON_EXISTENT = "nonExistentPlayer";
  private static final UUID MOLE_ID_EXISTENT = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MOLE_ID_NON_EXISTENT = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final String EXCEPTION_EXPECTED = "Exception should have been thrown";
  private static final String URI_BASE = "http://localhost:";
  private GameServiceClient gameServiceClient;
  @Value("${stubrunner.runningstubs.game-server.port}") private int port;

  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    gameServiceClient = new GameServiceClient(URI_BASE + port + "/players", URI_BASE + port + "/game/moles");
  }

  @Test
  public void shouldRegisterPlayer() throws ExecutionException, InterruptedException
  {
    gameServiceClient.register(PLAYER_NAME_NON_EXISTENT).toCompletableFuture().get();
  }

  @Test
  public void shouldNotRegisterAlreadyRegisteredPlayer() throws InterruptedException
  {
    try
    {
      gameServiceClient.register(PLAYER_NAME_EXISTENT).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerAlreadyRegisteredException.class, e.getCause().getClass());
    }
  }

  @Test
  public void shouldRemoveAlreadyRegisterPlayer() throws ExecutionException, InterruptedException
  {
    gameServiceClient.deregister(PLAYER_NAME_EXISTENT).toCompletableFuture().get();
  }

  @Test
  public void shouldNotRemoveNonExistentPlayer() throws InterruptedException
  {
    try
    {
      gameServiceClient.deregister(PLAYER_NAME_NON_EXISTENT).toCompletableFuture().get();
      fail(EXCEPTION_EXPECTED);
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerNotRegisteredException.class, e.getCause().getClass());
    }
  }

  @Test
  public void shouldListAllActiveMoles() throws ExecutionException, InterruptedException
  {
    var moles = gameServiceClient.activeMoles().toCompletableFuture().get();
    assertEquals(Mole.class, moles.get(0).getClass());
  }

  @Test
  public void shouldReturnTrueForSuccessfullyWhackedMole() throws ExecutionException, InterruptedException
  {
    var isGoodWhack = gameServiceClient.whack(PLAYER_NAME_EXISTENT, new Mole(MOLE_ID_EXISTENT))
                                       .toCompletableFuture()
                                       .get();
    assertEquals(Boolean.TRUE, isGoodWhack);
  }

  @Test
  public void shouldReturnFalseForUnsuccessfullyWhackedMole() throws ExecutionException, InterruptedException
  {
    var isGoodWhack = gameServiceClient.whack(PLAYER_NAME_EXISTENT, new Mole(MOLE_ID_NON_EXISTENT))
                                       .toCompletableFuture()
                                       .get();
    assertEquals(Boolean.FALSE, isGoodWhack);
  }

  @Test
  public void shouldNotSuccessfullyWhackMoleForNonExistentPlayer() throws InterruptedException
  {
    try
    {
      gameServiceClient.whack(PLAYER_NAME_NON_EXISTENT, new Mole(MOLE_ID_EXISTENT)).toCompletableFuture().get();
    }
    catch (ExecutionException e)
    {
      assertEquals(PlayerNotRegisteredException.class, e.getCause().getClass());
    }
  }
}
