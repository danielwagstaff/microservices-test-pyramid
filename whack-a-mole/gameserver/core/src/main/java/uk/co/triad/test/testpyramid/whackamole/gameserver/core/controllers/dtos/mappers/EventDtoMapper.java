package uk.co.triad.test.testpyramid.whackamole.gameserver.core.controllers.dtos.mappers;

import uk.co.triad.test.testpyramid.whackamole.gameserver.api.controllers.dtos.EventDto;

public class EventDtoMapper
{
  private EventDtoMapper()
  {
    // static class
  }

  public static EventDto createDto(String event)
  {
    return new EventDto(event);
  }
}
