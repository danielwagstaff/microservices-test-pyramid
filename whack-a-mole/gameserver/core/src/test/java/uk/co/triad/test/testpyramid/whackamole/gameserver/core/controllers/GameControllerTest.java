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
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.*;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Mole;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.PlayerNotRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Score;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.services.GameCommandQueryService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerTest
{
  private static final String URI_BASE = "/game";
  private static final String URI_START = URI_BASE + "/start";
  private static final String URI_STOP = URI_BASE + "/stop";
  private static final String URI_MOLES = URI_BASE + "/moles";
  private static final String URI_EVENTS = URI_BASE + "/events";
  private static final String URI_SCORES = URI_BASE + "/scores";
  private static final PlayerNameDto MOCK_PLAYER_1_NAME_DTO = new PlayerNameDto("dan");
  private static final PlayerNameDto MOCK_PLAYER_2_NAME_DTO = new PlayerNameDto("dave");

  private static final PlayerNameDto MOCK_PLAYER_NAME_DTO = new PlayerNameDto(MOCK_PLAYER_1_NAME_DTO.getPlayerName());
  private static final MoleIdDto MOCK_MOLE_DTO = new MoleIdDto(Mole.newMole().getId());
  private static final WhackDto MOCK_WHACK_DTO = new WhackDto(MOCK_PLAYER_NAME_DTO, MOCK_MOLE_DTO);

  @Mock private GameCommandQueryService mockedGameCommandQueryService;
  private MockMvc mockMvc;

  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    var gameController = new GameController(mockedGameCommandQueryService);
    this.mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();
  }

  @Test
  public void shouldReturnOkWhenGameStarted() throws Exception
  {
    when(mockedGameCommandQueryService.start()).thenReturn(CompletableFuture.completedFuture(null));

    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_START)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk());

    verify(mockedGameCommandQueryService, times(1)).start();
  }

  @Test
  public void shouldReturnOkWhenGameStopped() throws Exception
  {
    when(mockedGameCommandQueryService.stop()).thenReturn(CompletableFuture.completedFuture(null));

    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_STOP)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk());

    verify(mockedGameCommandQueryService, times(1)).stop();
  }

  @Test
  public void shouldReturnOkAndListOfMoles() throws Exception
  {
    var mole1 = Mole.newMole();
    var mole2 = Mole.newMole();
    var moles = List.of(mole1, mole2);

    when(mockedGameCommandQueryService.activeMoles()).thenReturn(CompletableFuture.completedFuture(moles));

    var expectedResult = new ObjectMapper().writeValueAsString(List.of(new MoleIdDto(mole1.getId()),
                                                                       new MoleIdDto(mole2.getId())));
    mockMvc.perform(asyncDispatch(mockMvc.perform(get(URI_MOLES)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk())
           .andExpect(content().json(expectedResult));

    verify(mockedGameCommandQueryService, times(1)).activeMoles();
  }

  @Test
  public void shouldReturnOkWhenMoleWhacked() throws Exception
  {
    when(mockedGameCommandQueryService.whack(any(Mole.class),
                                             any(Player.class))).thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

    var postBody = new ObjectMapper().writeValueAsString(MOCK_WHACK_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_MOLES).content(postBody)
                                                                 .contentType(MediaType.APPLICATION_JSON)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk());

    verify(mockedGameCommandQueryService, times(1)).whack(any(Mole.class), any(Player.class));
  }

  @Test
  public void shouldReturnNotFoundForAttemptedWhackOnNonExistentMole() throws Exception
  {
    when(mockedGameCommandQueryService.whack(any(Mole.class),
                                             any(Player.class))).thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));

    var postBody = new ObjectMapper().writeValueAsString(MOCK_WHACK_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_MOLES).content(postBody)
                                                                 .contentType(MediaType.APPLICATION_JSON)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isNotFound());

    verify(mockedGameCommandQueryService, times(1)).whack(any(Mole.class), any(Player.class));
  }

  @Test
  public void shouldReturnBadRequestWhenNonPlayerAttemptsToWhackMole() throws Exception
  {
    var failFlow = new CompletableFuture<Boolean>();
    failFlow.completeExceptionally(new PlayerNotRegisteredException("TestNonExistentPlayer"));
    when(mockedGameCommandQueryService.whack(any(Mole.class), any(Player.class))).thenReturn(failFlow);

    var postBody = new ObjectMapper().writeValueAsString(MOCK_WHACK_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_MOLES).content(postBody)
                                                                 .contentType(MediaType.APPLICATION_JSON)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isBadRequest());

    verify(mockedGameCommandQueryService, times(1)).whack(any(Mole.class), any(Player.class));
  }

  @Test
  public void shouldReturnInternalServerErrorForWhackMoleUnexpectedException() throws Exception
  {
    var failFlow = new CompletableFuture<Boolean>();
    failFlow.completeExceptionally(new Exception("You didn't expect me, did you?"));
    when(mockedGameCommandQueryService.whack(any(Mole.class), any(Player.class))).thenReturn(failFlow);

    var postBody = new ObjectMapper().writeValueAsString(MOCK_WHACK_DTO);
    mockMvc.perform(asyncDispatch(mockMvc.perform(post(URI_MOLES).content(postBody)
                                                                 .contentType(MediaType.APPLICATION_JSON)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isInternalServerError());

    verify(mockedGameCommandQueryService, times(1)).whack(any(Mole.class), any(Player.class));
  }

  @Test
  public void shouldReturnOkAndListOfGameEvents() throws Exception
  {
    var event1 = "event1";
    var event2 = "event2";
    var event3 = "event3";
    var events = List.of(event1, event2, event3);
    when(mockedGameCommandQueryService.getUnreadEvents()).thenReturn(CompletableFuture.completedFuture(events));

    var expectedResult = new ObjectMapper().writeValueAsString(List.of(new EventDto(event1),
                                                                       new EventDto(event2),
                                                                       new EventDto(event3)));
    mockMvc.perform(asyncDispatch(mockMvc.perform(get(URI_EVENTS)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk())
           .andExpect(content().json(expectedResult));

    verify(mockedGameCommandQueryService, times(1)).getUnreadEvents();
  }

  @Test
  public void shouldReturnListOfPlayersScores() throws Exception
  {
    var player1 = Player.newPlayer(MOCK_PLAYER_1_NAME_DTO.getPlayerName());
    var player1Score = Score.newScore();
    player1Score.increment(10);
    player1.setScore(player1Score);
    var player2 = Player.newPlayer(MOCK_PLAYER_2_NAME_DTO.getPlayerName());
    var player2Score = Score.newScore();
    player2Score.increment(20);
    player2.setScore(player2Score);
    var players = List.of(player1, player2);

    when(mockedGameCommandQueryService.activePlayers()).thenReturn(CompletableFuture.completedFuture(players));

    var expectedResult = new ObjectMapper().writeValueAsString(List.of(new PlayerScoreDto(player1.getName(),
                                                                                          player1.getScore()
                                                                                                 .getTotal()),
                                                                       new PlayerScoreDto(player2.getName(),
                                                                                          player2.getScore()
                                                                                                 .getTotal())));
    mockMvc.perform(asyncDispatch(mockMvc.perform(get(URI_SCORES)).andReturn()))
           .andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk())
           .andExpect(content().json(expectedResult));

    verify(mockedGameCommandQueryService, times(1)).activePlayers();
  }
}
