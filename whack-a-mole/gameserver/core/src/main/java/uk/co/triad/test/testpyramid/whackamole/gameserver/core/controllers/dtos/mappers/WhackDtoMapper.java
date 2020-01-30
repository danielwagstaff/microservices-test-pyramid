package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.WhackDto;

import java.util.UUID;

public class WhackDtoMapper
{
  private WhackDtoMapper()
  {
    // static class
  }

  public static String createPlayerName(WhackDto whackDto)
  {
    return whackDto.getPlayerNameDto().getPlayerName();
  }

  public static UUID createMoleId(WhackDto whackDto)
  {
    return whackDto.getMoleIdDto().getMoleId();
  }
}
