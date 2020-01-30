package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.contracts.gameclient;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.GameController;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Mole;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerNotRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.services.GameCommandQueryService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class GameContractBaseTest
{
  @Mock private GameCommandQueryService mockedGameCommandQueryService;

  @Before
  public void setUp()
  {
    RestAssuredMockMvc.standaloneSetup(new GameController(mockedGameCommandQueryService));
    mockListMoles();
    mockWhackMole();
  }

  private void mockListMoles()
  {
    var mole1 = Mole.newMole();
    var mole2 = Mole.newMole();
    var moles = List.of(mole1, mole2);
    when(mockedGameCommandQueryService.activeMoles()).thenReturn(CompletableFuture.completedFuture(moles));
  }

  private void mockWhackMole()
  {
    when(mockedGameCommandQueryService.whack(any(Mole.class),
                                             any(Player.class))).thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

    var nonExistentMoleId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    when(mockedGameCommandQueryService.whack(eq(Mole.newMole(nonExistentMoleId)), any(Player.class))).thenReturn(
        CompletableFuture.completedFuture(Boolean.FALSE));

    var failWhack = new CompletableFuture<Boolean>();
    failWhack.completeExceptionally(new PlayerNotRegisteredException("TestNonExistentPlayer"));
    when(mockedGameCommandQueryService.whack(any(Mole.class), eq(Player.newPlayer("nonExistentPlayer")))).thenReturn(
        failWhack);
  }
}
