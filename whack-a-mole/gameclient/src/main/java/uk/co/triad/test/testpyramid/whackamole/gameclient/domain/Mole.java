package uk.co.triad.test.testpyramid.whackamole.gameclient.domain;

import java.util.UUID;

public class Mole
{
  private UUID id;

  public Mole()
  {
    // (de)serialization
  }

  public Mole(UUID id)
  {
    this.setId(id);
  }

  public UUID getId()
  {
    return id;
  }

  public void setId(UUID id)
  {
    this.id = id;
  }
}
