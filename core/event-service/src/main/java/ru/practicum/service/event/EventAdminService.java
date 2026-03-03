package ru.practicum.service.event;


import ru.practicum.dal.dto.event.EventAdminFilter;
import ru.practicum.dal.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.EventFullDto;

import java.util.List;

public interface EventAdminService {

    List<EventFullDto> getAll(EventAdminFilter adminFilter, Integer from, Integer size);

    EventFullDto update(UpdateEventAdminRequest request, Long eventId);
}
