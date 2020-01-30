package uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos;

public class PlayerScoreDto
{
  private String playerName;
  private int score;

  public PlayerScoreDto()
  {
    // (de)serialization
  }

  public PlayerScoreDto(String playerName, int score)
  {
    this.setPlayerName(playerName);
    this.setScore(score);
  }

  public String getPlayerName()
  {
    return playerName;
  }

  public void setPlayerName(String playerName)
  {
    this.playerName = playerName;
  }

  public int getScore()
  {
    return score;
  }

  public void setScore(int score)
  {
    this.score = score;
  }
}
