package uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos;

public class PlayerNameDto
{
  private String playerName;

  public PlayerNameDto()
  {
    // (de)serialization
  }

  public PlayerNameDto(String playerName)
  {
    this.setPlayerName(playerName);
  }

  public String getPlayerName()
  {
    return playerName;
  }

  public void setPlayerName(String playerName)
  {
    this.playerName = playerName;
  }
}
