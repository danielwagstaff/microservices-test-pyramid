package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerScoreDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;

public class PlayerScoreDtoMapper
{
  private PlayerScoreDtoMapper()
  {
    // static class
  }

  public static PlayerScoreDto createDto(Player player)
  {
    return new PlayerScoreDto(player.getName(), player.getScore().getTotal());
  }
}
