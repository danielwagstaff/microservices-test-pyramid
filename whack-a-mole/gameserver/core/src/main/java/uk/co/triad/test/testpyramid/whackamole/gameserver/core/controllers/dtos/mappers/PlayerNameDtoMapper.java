package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerNameDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;

public class PlayerNameDtoMapper
{
  private PlayerNameDtoMapper()
  {
    // static class
  }

  public static PlayerNameDto createDto(Player player)
  {
    return new PlayerNameDto(player.getName());
  }
}
