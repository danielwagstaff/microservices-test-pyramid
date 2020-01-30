package uk.co.triad.test.testpyramid.whackamole.gameclient.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.triad.test.testpyramid.whackamole.gameclient.services.PlayerCommandQueryService;

import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping("play")
public class PlayController
{
  private static final Logger LOGGER = LoggerFactory.getLogger(PlayController.class);
  private PlayerCommandQueryService playerCommandQueryService;

  @Autowired
  public PlayController(PlayerCommandQueryService playerCommandQueryService)
  {
    this.playerCommandQueryService = playerCommandQueryService;
  }

  @PostMapping(path = "start")
  public CompletionStage<ResponseEntity<Void>> start()
  {
    LOGGER.info("Start game");
    return playerCommandQueryService.startPlaying().handle((res, err) -> {
      if (err == null)
      {
        return new ResponseEntity<>(HttpStatus.OK);
      }
      else
      {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    });
  }

  @PostMapping(path = "stop")
  public CompletionStage<ResponseEntity<Void>> stop()
  {
    LOGGER.info("Stop game");
    return playerCommandQueryService.stopPlaying().handle((res, err) -> {
      if (err == null)
      {
        return new ResponseEntity<>(HttpStatus.OK);
      }
      else
      {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    });
  }
}
