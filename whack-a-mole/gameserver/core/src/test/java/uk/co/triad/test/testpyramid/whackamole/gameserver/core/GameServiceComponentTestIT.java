package uk.co.triad.test.testpyramid.whackamole.gameserver.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.MoleIdDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerNameDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerScoreDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.WhackDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Mole;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Score;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * As a stressed-out programmer,
 * I want to whack some moles virtually,
 * so I can blow off some steam without getting off my chair
 */
public class GameServiceComponentTestIT
{
  private static final String EXCEPTION_RESP_UNSUCCESSFUL = "Response should be successful";
  private static final String URI_BASE = "http://localhost:" + System.getProperty("test.server.port");
  private static final String URI_REGISTER = URI_BASE + "/players";
  private static final String URI_GAME = URI_BASE + "/game";
  private static final String URI_GAME_START = URI_GAME + "/start";
  private static final String URI_GAME_STOP = URI_GAME + "/stop";
  private static final String URI_MOLES = URI_GAME + "/moles";
  private static final String URI_SCORES = URI_GAME + "/scores";
  private static final String PLAYER_NAME = "player-one";
  private final HttpClient httpClient = HttpClient.newBuilder().build();

  @After
  public void tearDown() throws InterruptedException, ExecutionException, JsonProcessingException
  {
    var stopGameResponse = stopGame();
    assertTrue(EXCEPTION_RESP_UNSUCCESSFUL, HttpStatus.valueOf(stopGameResponse.statusCode()).is2xxSuccessful());

    var leaveGameResponse = leaveGame();
    assertTrue(EXCEPTION_RESP_UNSUCCESSFUL, HttpStatus.valueOf(leaveGameResponse.statusCode()).is2xxSuccessful());

    assertAllPlayersLeft();
  }

  /**
   * Scenario:
   * Successfully whack some moles
   */
  @Test
  public void shouldWhackSomeMoles() throws ExecutionException, InterruptedException, JsonProcessingException
  {
    // GIVEN
    successfullyJoinedGame();
    successfullyStartedGame();

    // WHEN
    var moleToWhack = getActiveMoles().get(0);
    var whackResponse = whackMole(moleToWhack);

    // THEN
    assertTrue(HttpStatus.valueOf(whackResponse.statusCode()).is2xxSuccessful());
    assertFalse(getActiveMoles().contains(moleToWhack));
    assertEquals(PLAYER_NAME, getPlayers().get(0).getName());
    assertTrue(getPlayers().get(0).getScore().getTotal() > 0);
  }

  /**
   * Scenario:
   * Cannot join game, because player already registered
   */
  @Test
  public void shouldNotJoinGameTwice() throws ExecutionException, InterruptedException, JsonProcessingException
  {
    // GIVEN
    successfullyJoinedGame();

    // WHEN
    var joinResponse = joinGame();

    // THEN
    assertEquals("Expected response: 409", HttpStatus.CONFLICT, HttpStatus.valueOf(joinResponse.statusCode()));
  }

  private HttpResponse<Void> joinGame() throws JsonProcessingException, ExecutionException, InterruptedException
  {
    var playerNameDto = new PlayerNameDto(PLAYER_NAME);
    var objectMapper = new ObjectMapper();
    var requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(playerNameDto);

    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_REGISTER))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).toCompletableFuture().get();
  }

  private void successfullyJoinedGame() throws InterruptedException, ExecutionException, JsonProcessingException
  {
    var joinResponse = joinGame();
    assertTrue(EXCEPTION_RESP_UNSUCCESSFUL, HttpStatus.valueOf(joinResponse.statusCode()).is2xxSuccessful());
  }

  private HttpResponse<Void> leaveGame() throws JsonProcessingException, ExecutionException, InterruptedException
  {
    var playerNameDto = new PlayerNameDto(PLAYER_NAME);
    var objectMapper = new ObjectMapper();
    var requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(playerNameDto);

    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_REGISTER))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody))
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).toCompletableFuture().get();
  }

  private HttpResponse<String> listActivePlayers() throws ExecutionException, InterruptedException
  {
    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_REGISTER))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .GET()
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).toCompletableFuture().get();
  }

  private List<String> responseToPlayerNames(final HttpResponse<String> resp) throws JsonProcessingException
  {
    var objectMapper = new ObjectMapper();
    var responseBody = objectMapper.readValue(resp.body(), PlayerNameDto[].class);
    return Arrays.stream(responseBody).map(PlayerNameDto::getPlayerName).collect(Collectors.toList());
  }

  private void assertAllPlayersLeft()
  {
    final List<String> playerNames = new ArrayList<>();
    await().atMost(Duration.ofSeconds(4)).until(() -> {
      var playersResponse = listActivePlayers();
      assertTrue(EXCEPTION_RESP_UNSUCCESSFUL, HttpStatus.valueOf(playersResponse.statusCode()).is2xxSuccessful());
      playerNames.addAll(responseToPlayerNames(playersResponse));
      return playerNames.isEmpty();
    });
    assertTrue("Should be no players registered", playerNames.isEmpty());
  }

  private HttpResponse<Void> startGame() throws ExecutionException, InterruptedException
  {
    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_GAME_START))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .POST(HttpRequest.BodyPublishers.noBody())
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).toCompletableFuture().get();
  }

  private void successfullyStartedGame() throws ExecutionException, InterruptedException
  {
    var startGameResponse = startGame();
    assertTrue(EXCEPTION_RESP_UNSUCCESSFUL, HttpStatus.valueOf(startGameResponse.statusCode()).is2xxSuccessful());
  }

  private HttpResponse<Void> stopGame() throws ExecutionException, InterruptedException
  {
    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_GAME_STOP))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .POST(HttpRequest.BodyPublishers.noBody())
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).toCompletableFuture().get();
  }

  private HttpResponse<String> listActiveMoles() throws ExecutionException, InterruptedException
  {
    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_MOLES))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .GET()
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).toCompletableFuture().get();
  }

  private List<Mole> responseToMoles(final HttpResponse<String> resp) throws JsonProcessingException
  {
    var objectMapper = new ObjectMapper();
    var responseBody = objectMapper.readValue(resp.body(), MoleIdDto[].class);
    return Arrays.stream(responseBody).map(moleDto -> Mole.newMole(moleDto.getMoleId())).collect(Collectors.toList());
  }

  private List<Mole> getActiveMoles()
  {
    final List<Mole> moles = new ArrayList<>();
    await().atMost(Duration.ofSeconds(4)).until(() -> {
      var molesResponse = listActiveMoles();
      assertTrue(EXCEPTION_RESP_UNSUCCESSFUL, HttpStatus.valueOf(molesResponse.statusCode()).is2xxSuccessful());
      moles.addAll(responseToMoles(molesResponse));
      return !moles.isEmpty();
    });
    return moles;
  }

  private HttpResponse<Void> whackMole(Mole mole) throws
                                                  JsonProcessingException,
                                                  ExecutionException,
                                                  InterruptedException
  {
    var playerNameDto = new PlayerNameDto(PLAYER_NAME);
    var moleIdDto = new MoleIdDto(mole.getId());
    var whackDto = new WhackDto(playerNameDto, moleIdDto);
    var objectMapper = new ObjectMapper();
    var requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(whackDto);

    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_MOLES))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).toCompletableFuture().get();
  }

  private HttpResponse<String> listPlayerScores() throws ExecutionException, InterruptedException
  {
    var request = HttpRequest.newBuilder()
                             .uri(URI.create(URI_SCORES))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .GET()
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).toCompletableFuture().get();
  }

  private List<Player> responseToPlayerScores(final HttpResponse<String> resp) throws JsonProcessingException
  {
    var objectMapper = new ObjectMapper();
    var responseBody = objectMapper.readValue(resp.body(), PlayerScoreDto[].class);
    return Arrays.stream(responseBody)
                 .map(playerScoreDto -> new Player(playerScoreDto.getPlayerName(), new Score(playerScoreDto.getScore())))
                 .collect(Collectors.toList());
  }

  private List<Player> getPlayers()
  {
    final List<Player> players = new ArrayList<>();
    await().atMost(Duration.ofSeconds(4)).until(() -> {
      var playerScoresResponse = listPlayerScores();
      assertTrue(EXCEPTION_RESP_UNSUCCESSFUL, HttpStatus.valueOf(playerScoresResponse.statusCode()).is2xxSuccessful());
      players.addAll(responseToPlayerScores(playerScoresResponse));
      return !players.isEmpty();
    });
    return players;
  }
}
