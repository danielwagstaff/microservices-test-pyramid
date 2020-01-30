package uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos;

public class EventDto
{
  private String event;

  public EventDto()
  {
    // (de)serialization
  }

  public EventDto(String event)
  {
    this.setEvent(event);
  }

  public String getEvent()
  {
    return event;
  }

  public void setEvent(String event)
  {
    this.event = event;
  }
}
