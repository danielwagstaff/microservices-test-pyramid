package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.MoleIdDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Mole;

public class MoleIdDtoMapper
{
  private MoleIdDtoMapper()
  {
    // static class
  }

  public static MoleIdDto createDto(Mole mole)
  {
    return new MoleIdDto(mole.getId());
  }
}
