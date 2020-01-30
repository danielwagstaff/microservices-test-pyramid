package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

public class Player
{
  private String name;
  private Score score;

  public Player()
  {
    // (de)serialization
  }

  public Player(String name, Score score)
  {
    this.setName(name);
    this.setScore(score);
  }

  public static Player newPlayer(String name)
  {
    return new Player(name, Score.newScore());
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public Score getScore()
  {
    return score;
  }

  public void setScore(Score score)
  {
    this.score = score;
  }

  @Override
  public int hashCode()
  {
    return this.getName().hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    if (other instanceof Player)
    {
      return this.getName().equals(((Player) other).getName());
    }
    else
    {
      return false;
    }
  }
}
