package uk.co.triad.test.testpyramid.whackamole.gameclient.domain;

public class PlayerLeaveGameFailedException extends Exception
{
  public PlayerLeaveGameFailedException(Throwable throwable)
  {
    super(throwable);
  }
}
