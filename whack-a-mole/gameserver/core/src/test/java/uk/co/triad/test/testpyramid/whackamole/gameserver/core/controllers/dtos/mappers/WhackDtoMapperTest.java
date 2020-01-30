package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import org.junit.Test;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.MoleIdDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.PlayerNameDto;
import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.WhackDto;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class WhackDtoMapperTest
{
  @Test
  public void shouldCreatePlayerNameFromDto()
  {
    var playerNameDto = new PlayerNameDto("dan");
    var moleIdDto = new MoleIdDto(UUID.randomUUID());
    var whackDto = new WhackDto(playerNameDto, moleIdDto);

    var expectedPlayerName = whackDto.getPlayerNameDto().getPlayerName();

    assertEquals(playerNameDto.getPlayerName(), expectedPlayerName);
  }

  @Test
  public void shouldCreateMoleIdFromDto()
  {
    var playerNameDto = new PlayerNameDto("dan");
    var moleIdDto = new MoleIdDto(UUID.randomUUID());
    var whackDto = new WhackDto(playerNameDto, moleIdDto);

    var expectedMoleId = whackDto.getMoleIdDto().getMoleId();

    assertEquals(moleIdDto.getMoleId(), expectedMoleId);
  }
}
