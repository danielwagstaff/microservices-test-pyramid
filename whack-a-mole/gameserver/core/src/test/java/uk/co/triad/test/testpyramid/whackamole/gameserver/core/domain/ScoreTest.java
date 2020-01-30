package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ScoreTest
{
  @Test
  public void shouldReturnNewScoreOfZero()
  {
    var score = Score.newScore();

    assertEquals(0, score.getTotal());
  }

  @Test
  public void shouldIncrementScoreByGivenAmount()
  {
    var score = Score.newScore();

    assertEquals(0, score.getTotal());

    score.increment(10);
    assertEquals(10, score.getTotal());
  }

  @Test
  public void shouldReturnHashCode()
  {
    var scoreTotal = 10;
    var score = new Score(scoreTotal);
    assertEquals(Integer.valueOf(scoreTotal).hashCode(), score.hashCode());
  }

  @Test
  public void shouldBeEqualWhenScoresEqual()
  {
    var equalScore1 = new Score(10);
    var equalScore2 = new Score(10);
    var unequalScore = new Score(20);

    assertNotEquals(equalScore1, unequalScore);
    assertEquals(equalScore1, equalScore2);
  }

  @Test
  public void shouldBeUnequalWhenDifferentObject()
  {
    var equalScore1 = new Score(10);

    assertNotEquals(equalScore1, new Object());
  }
}
