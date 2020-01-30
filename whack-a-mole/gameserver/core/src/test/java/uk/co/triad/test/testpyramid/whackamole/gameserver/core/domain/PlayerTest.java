package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PlayerTest
{
  private final static String MOCK_PLAYER_NAME = "dan";

  @Test
  public void shouldReturnNewPlayerWithSuppliedNameAndScoreOfZero()
  {
    var player = Player.newPlayer(MOCK_PLAYER_NAME);

    assertEquals(MOCK_PLAYER_NAME, player.getName());
    assertEquals(0, player.getScore().getTotal());
  }

  @Test
  public void shouldReturnHashCode()
  {
    var player = Player.newPlayer(MOCK_PLAYER_NAME);
    assertEquals(MOCK_PLAYER_NAME.hashCode(), player.hashCode());
  }

  @Test
  public void shouldBeEqualWhenNamesEqual()
  {
    var equalPlayer1 = Player.newPlayer(MOCK_PLAYER_NAME);
    var equalPlayer2 = Player.newPlayer(MOCK_PLAYER_NAME);
    var unequalPlayer = Player.newPlayer("dave");

    equalPlayer1.getScore().setTotal(0);
    equalPlayer2.getScore().setTotal(10);
    unequalPlayer.getScore().setTotal(0);

    assertNotEquals(equalPlayer1, unequalPlayer);
    assertEquals(equalPlayer1, equalPlayer2);
  }

  @Test
  public void shouldBeUnequalWhenDifferentObject()
  {
    var equalPlayer1 = Player.newPlayer(MOCK_PLAYER_NAME);

    assertNotEquals(equalPlayer1, new Object());
  }
}
