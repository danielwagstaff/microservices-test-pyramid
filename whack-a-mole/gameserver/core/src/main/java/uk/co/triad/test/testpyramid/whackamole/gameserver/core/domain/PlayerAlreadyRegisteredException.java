package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

public class PlayerAlreadyRegisteredException extends Exception
{
  public PlayerAlreadyRegisteredException(String message)
  {
    super(message);
  }
}
