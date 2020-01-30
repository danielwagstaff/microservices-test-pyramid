package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.contracts.gameclient;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.RegistrationController;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerAlreadyRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerNotRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.services.GameCommandQueryService;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class RegistrationContractBaseTest
{
  @Mock private GameCommandQueryService mockedGameCommandQueryService;

  @Before
  public void setUp()
  {
    RestAssuredMockMvc.standaloneSetup(new RegistrationController(mockedGameCommandQueryService));
    mockRegister();
    mockDeregister();
  }

  private void mockRegister()
  {
    when(mockedGameCommandQueryService.register(any(Player.class))).thenReturn(CompletableFuture.completedFuture(null));

    var failRegistration = new CompletableFuture<Void>();
    failRegistration.completeExceptionally(new PlayerAlreadyRegisteredException("TestDuplicatedPlayer"));
    when(mockedGameCommandQueryService.register(Player.newPlayer("existentPlayer"))).thenReturn(failRegistration);
  }

  private void mockDeregister()
  {
    when(mockedGameCommandQueryService.remove(any(Player.class))).thenReturn(CompletableFuture.completedFuture(null));

    var failDeregistration = new CompletableFuture<Void>();
    failDeregistration.completeExceptionally(new PlayerNotRegisteredException("TestNonExistentPlayer"));
    when(mockedGameCommandQueryService.remove(Player.newPlayer("nonExistentPlayer"))).thenReturn(failDeregistration);
  }
}
