package ru.practicum.service.event;


import ru.practicum.dal.dto.event.EventShortDto;
import ru.practicum.dal.dto.event.NewEventDto;
import ru.practicum.dal.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.EventFullDto;

import java.util.List;

public interface EventPrivateService {

    List<EventShortDto> getAll(Long userId, Integer from, Integer size);

    EventFullDto create(NewEventDto newEventDto, Long userId);

    EventFullDto getByInitiatorId(Long userId, Long eventId);

    EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId);

}
