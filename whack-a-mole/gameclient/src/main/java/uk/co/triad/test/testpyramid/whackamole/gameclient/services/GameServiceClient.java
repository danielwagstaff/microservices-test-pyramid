package uk.co.triad.test.testpyramid.whackamole.gameclient.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.Mole;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.PlayerAlreadyRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.PlayerNotRegisteredException;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.MoleIdDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerNameDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.WhackDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class GameServiceClient implements GameService
{
  private static final String UNHANDLED_EXCEPTION = "Unhandled exception";
  private final String uriPlayers;
  private final String uriMoles;
  private HttpClient httpClient = HttpClient.newHttpClient();

  @Autowired
  public GameServiceClient(@Value("${services.gameserver.uri.players}") final String uriPlayers,
                           @Value("${services.gameserver.uri.game.moles}") final String uriMoles)
  {
    this.uriPlayers = uriPlayers;
    this.uriMoles = uriMoles;
  }

  @Override
  public CompletionStage<Void> register(String playerName)
  {
    try
    {
      var playerNameDto = new PlayerNameDto(playerName);
      var objectMapper = new ObjectMapper();
      var requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(playerNameDto);

      var request = HttpRequest.newBuilder()
                               .uri(URI.create(uriPlayers))
                               .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                               .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                               .build();

      return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).exceptionally(t -> {
        throw new CompletionException(t);
      }).thenAccept(resp -> {
        if (!HttpStatus.valueOf(resp.statusCode()).is2xxSuccessful())
        {
          if (HttpStatus.CONFLICT.value() == resp.statusCode())
          {
            throw new CompletionException(new PlayerAlreadyRegisteredException());
          }
          else
          {
            throw new CompletionException(new Exception(UNHANDLED_EXCEPTION));
          }
        }
      });
    }
    catch (JsonProcessingException e)
    {
      throw new CompletionException(e);
    }
  }

  @Override
  public CompletionStage<Void> deregister(String playerName)
  {
    try
    {
      var playerNameDto = new PlayerNameDto(playerName);
      var objectMapper = new ObjectMapper();
      var requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(playerNameDto);

      var request = HttpRequest.newBuilder()
                               .uri(URI.create(uriPlayers))
                               .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                               .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody))
                               .build();

      return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).exceptionally(t -> {
        throw new CompletionException(t);
      }).thenAccept(resp -> {
        if (!HttpStatus.valueOf(resp.statusCode()).is2xxSuccessful())
        {
          if (HttpStatus.BAD_REQUEST.value() == resp.statusCode())
          {
            throw new CompletionException(new PlayerNotRegisteredException("Player not registered"));
          }
          else
          {
            throw new CompletionException(new Exception(UNHANDLED_EXCEPTION));
          }
        }
      });
    }
    catch (JsonProcessingException e)
    {
      throw new CompletionException(e);
    }
  }

  @Override
  public CompletionStage<List<Mole>> activeMoles()
  {
    var request = HttpRequest.newBuilder()
                             .uri(URI.create(uriMoles))
                             .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                             .GET()
                             .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).exceptionally(t -> {
      throw new CompletionException(t.getCause());
    }).thenApply(resp -> {
      if (HttpStatus.valueOf(resp.statusCode()).is2xxSuccessful())
      {
        try
        {
          var objectMapper = new ObjectMapper();
          var responseBody = objectMapper.readValue(resp.body(), MoleIdDto[].class);
          return Arrays.stream(responseBody).map(moleDto -> new Mole(moleDto.getMoleId())).collect(Collectors.toList());
        }
        catch (JsonProcessingException e)
        {
          throw new CompletionException(e);
        }
      }
      else
      {
        throw new CompletionException(new Exception(UNHANDLED_EXCEPTION));
      }
    });
  }

  @Override
  public CompletionStage<Boolean> whack(String playerName, Mole mole)
  {
    try
    {
      var playerNameDto = new PlayerNameDto(playerName);
      var moleIdDto = new MoleIdDto(mole.getId());
      var whackDto = new WhackDto(playerNameDto, moleIdDto);
      var objectMapper = new ObjectMapper();
      var requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(whackDto);

      var request = HttpRequest.newBuilder()
                               .uri(URI.create(uriMoles))
                               .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                               .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                               .build();

      return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).exceptionally(t -> {
        throw new CompletionException(t.getCause());
      }).thenApply(resp -> {
        if (HttpStatus.valueOf(resp.statusCode()).is2xxSuccessful())
        {
          return Boolean.TRUE;
        }
        else if (resp.statusCode() == HttpStatus.NOT_FOUND.value())
        {
          return Boolean.FALSE;
        }
        else if (HttpStatus.BAD_REQUEST.value() == resp.statusCode())
        {
          throw new CompletionException(new PlayerNotRegisteredException("Player not registered"));
        }
        else
        {
          throw new CompletionException(new Exception(UNHANDLED_EXCEPTION));
        }
      });
    }
    catch (JsonProcessingException e)
    {
      throw new CompletionException(e);
    }
  }
}
