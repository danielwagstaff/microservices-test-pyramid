package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerNameDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerAlreadyRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerNotRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.services.GameCommandQueryService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerTest
{
  private static final PlayerNameDto MOCK_PLAYER_1_NAME_DTO = new PlayerNameDto("dan");
  private static final PlayerNameDto MOCK_PLAYER_2_NAME_DTO = new PlayerNameDto("dave");
  private static final String URI_BASE = "/players";
  @Mock private GameCommandQueryService mockedGameCommandQueryService;
  private MockMvc mockMvc;

  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    var registrationController = new RegistrationController(mockedGameCommandQueryService);
    this.mockMvc = MockMvcBuilders.standaloneSetup(registrationController).build();
  }

  @Test
  public void shouldReturnOkWhenNewPlayerRegistered() throws Exception
  {
    when(mockedGameCommandQueryService.register(any(Player.class))).thenReturn(CompletableFuture.completedFuture(null));

    var postBody = new ObjectMapper().writeValueAsString(MOCK_PLAYER_1_NAME_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_BASE).content(postBody)
                                                                .contentType(MediaType.APPLICATION_JSON)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk());

    verify(mockedGameCommandQueryService, times(1)).register(any(Player.class));
  }

  @Test
  public void shouldReturnConflictWhenPlayerDoubleRegistered() throws Exception
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new PlayerAlreadyRegisteredException("TestDuplicatedPlayer"));
    when(mockedGameCommandQueryService.register(any(Player.class))).thenReturn(failFlow);

    var postBody = new ObjectMapper().writeValueAsString(MOCK_PLAYER_1_NAME_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_BASE).content(postBody)
                                                                .contentType(MediaType.APPLICATION_JSON)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isConflict());

    verify(mockedGameCommandQueryService, times(1)).register(any(Player.class));
  }

  @Test
  public void shouldReturnServerErrorForRegisterUnexpectedException() throws Exception
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new Exception("You didn't expect me, did you?"));
    when(mockedGameCommandQueryService.register(any(Player.class))).thenReturn(failFlow);

    var postBody = new ObjectMapper().writeValueAsString(MOCK_PLAYER_1_NAME_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_BASE).content(postBody)
                                                                .contentType(MediaType.APPLICATION_JSON)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isInternalServerError());

    verify(mockedGameCommandQueryService, times(1)).register(any(Player.class));
  }

  @Test
  public void shouldReturnOkWhenPlayerDeregistered() throws Exception
  {
    when(mockedGameCommandQueryService.remove(any(Player.class))).thenReturn(CompletableFuture.completedFuture(null));

    var postBody = new ObjectMapper().writeValueAsString(MOCK_PLAYER_1_NAME_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(delete(URI_BASE).content(postBody)
                                                                  .contentType(MediaType.APPLICATION_JSON))
                                         .andReturn())).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk());

    verify(mockedGameCommandQueryService, times(1)).remove(any(Player.class));
  }

  @Test
  public void shouldReturnBadRequestWhenNonPlayerDeregistered() throws Exception
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new PlayerNotRegisteredException("TestNonExistentPlayer"));
    when(mockedGameCommandQueryService.remove(any(Player.class))).thenReturn(failFlow);

    var postBody = new ObjectMapper().writeValueAsString(MOCK_PLAYER_1_NAME_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(delete(URI_BASE).content(postBody)
                                                                  .contentType(MediaType.APPLICATION_JSON))
                                         .andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isBadRequest());

    verify(mockedGameCommandQueryService, times(1)).remove(any(Player.class));
  }

  @Test
  public void shouldReturnServerErrorForDeregisterUnexpectedException() throws Exception
  {
    var failFlow = new CompletableFuture<Void>();
    failFlow.completeExceptionally(new Exception("You didn't expect me, did you?"));
    when(mockedGameCommandQueryService.remove(any(Player.class))).thenReturn(failFlow);

    var postBody = new ObjectMapper().writeValueAsString(MOCK_PLAYER_1_NAME_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(delete(URI_BASE).content(postBody)
                                                                  .contentType(MediaType.APPLICATION_JSON))
                                         .andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isInternalServerError());

    verify(mockedGameCommandQueryService, times(1)).remove(any(Player.class));
  }

  @Test
  public void shouldReturnListOfPlayers() throws Exception
  {
    var player1 = Player.newPlayer(MOCK_PLAYER_1_NAME_DTO.getPlayerName());
    var player2 = Player.newPlayer(MOCK_PLAYER_2_NAME_DTO.getPlayerName());
    var players = List.of(player1, player2);

    when(mockedGameCommandQueryService.activePlayers()).thenReturn(CompletableFuture.completedFuture(players));

    var expectedResult = new ObjectMapper().writeValueAsString(List.of(new PlayerNameDto(player1.getName()),
                                                                       new PlayerNameDto(player2.getName())));
    mockMvc.perform(asyncDispatch(mockMvc.perform(get(URI_BASE)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk())
           .andExpect(content().json(expectedResult));

    verify(mockedGameCommandQueryService, times(1)).activePlayers();
  }
}
