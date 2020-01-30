package uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos;

import java.util.UUID;

public class MoleIdDto
{
  private UUID moleId;

  public MoleIdDto()
  {
    // (de)serialization
  }

  public MoleIdDto(UUID moleId)
  {
    this.setMoleId(moleId);
  }

  public UUID getMoleId()
  {
    return moleId;
  }

  public void setMoleId(UUID moleId)
  {
    this.moleId = moleId;
  }
}
