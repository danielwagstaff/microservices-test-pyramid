package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

import java.util.UUID;

public class Mole
{
  private UUID id;

  public static Mole newMole()
  {
    return Mole.newMole(UUID.randomUUID());
  }

  public static Mole newMole(UUID id)
  {
    var mole = new Mole();
    mole.id = id;
    return mole;
  }

  public UUID getId()
  {
    return id;
  }

  @Override
  public int hashCode()
  {
    return this.id.hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    if (other instanceof Mole)
    {
      return this.id.equals(((Mole) other).id);
    }
    else
    {
      return false;
    }
  }
}
