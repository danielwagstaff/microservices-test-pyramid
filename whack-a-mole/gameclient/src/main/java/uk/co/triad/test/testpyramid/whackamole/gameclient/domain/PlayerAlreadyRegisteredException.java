package uk.co.triad.test.testpyramid.whackamole.gameclient.domain;

public class PlayerAlreadyRegisteredException extends Exception
{
  public PlayerAlreadyRegisteredException()
  {
    super();
  }

  public PlayerAlreadyRegisteredException(String msg)
  {
    super(msg);
  }
}
