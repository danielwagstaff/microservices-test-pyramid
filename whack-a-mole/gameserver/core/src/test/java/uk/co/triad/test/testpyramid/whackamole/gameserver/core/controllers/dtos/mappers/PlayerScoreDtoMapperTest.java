package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import org.junit.Test;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Score;

import static org.junit.Assert.assertEquals;

public class PlayerScoreDtoMapperTest
{
  @Test
  public void shouldCreateDtoFromPlayerScore()
  {
    var player = Player.newPlayer("dan");
    player.setScore(new Score(10));
    var playerScoreDto = PlayerScoreDtoMapper.createDto(player);
    assertEquals(player.getScore().getTotal(), playerScoreDto.getScore());
  }
}
