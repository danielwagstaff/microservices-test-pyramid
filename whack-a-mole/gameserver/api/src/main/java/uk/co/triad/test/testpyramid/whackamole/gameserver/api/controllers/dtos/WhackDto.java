package uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos;

public class WhackDto
{
  private PlayerNameDto playerNameDto;
  private MoleIdDto moleIdDto;

  public WhackDto()
  {
    // (de)serialization
  }

  public WhackDto(PlayerNameDto playerNameDto, MoleIdDto moleIdDto)
  {
    this.playerNameDto = playerNameDto;
    this.moleIdDto = moleIdDto;
  }

  public PlayerNameDto getPlayerNameDto()
  {
    return playerNameDto;
  }

  public void setPlayerNameDto(PlayerNameDto playerDto)
  {
    this.playerNameDto = playerDto;
  }

  public MoleIdDto getMoleIdDto()
  {
    return moleIdDto;
  }

  public void setMoleIdDto(MoleIdDto moleIdDto)
  {
    this.moleIdDto = moleIdDto;
  }
}
