package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import org.junit.Test;
import uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain.Player;

import static org.junit.Assert.assertEquals;

public class PlayerNameDtoMapperTest
{
  @Test
  public void shouldCreateDtoFromPlayerName()
  {
    var player = Player.newPlayer("dan");
    var playerNameDto = PlayerNameDtoMapper.createDto(player);
    assertEquals(player.getName(), playerNameDto.getPlayerName());
  }
}
