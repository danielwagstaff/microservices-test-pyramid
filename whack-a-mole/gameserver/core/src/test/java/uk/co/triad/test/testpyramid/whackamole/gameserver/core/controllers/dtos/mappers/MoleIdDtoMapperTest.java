package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import org.junit.Test;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Mole;

import static org.junit.Assert.assertEquals;

public class MoleIdDtoMapperTest
{
  @Test
  public void shouldCreateDtoFromMole()
  {
    var mole = Mole.newMole();
    var moleDto = MoleIdDtoMapper.createDto(mole);
    assertEquals(mole.getId(), moleDto.getMoleId());
  }
}
