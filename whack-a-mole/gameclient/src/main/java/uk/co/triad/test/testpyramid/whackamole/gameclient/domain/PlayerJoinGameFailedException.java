package uk.co.triad.test.testpyramid.whackamole.gameclient.domain;

public class PlayerJoinGameFailedException extends Exception
{
  public PlayerJoinGameFailedException(Throwable throwable)
  {
    super(throwable);
  }
}
