package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

public class Score
{
  private int total;

  public Score()
  {
    // (de)serialization
  }

  public Score(int total)
  {
    this.setTotal(total);
  }

  public static Score newScore()
  {
    return new Score(0);
  }

  public int getTotal()
  {
    return total;
  }

  public void setTotal(int total)
  {
    this.total = total;
  }

  public void increment(int increment)
  {
    this.total += increment;
  }

  @Override
  public int hashCode()
  {
    return Integer.valueOf(this.getTotal()).hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    if (other instanceof Score)
    {
      return Integer.valueOf(this.getTotal()).equals(((Score) other).getTotal());
    }
    else
    {
      return false;
    }
  }
}
