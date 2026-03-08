package ru.practicum.service.event;

import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dto.event.EventPublicFilter;
import ru.practicum.dal.dto.event.EventShortDto;
import ru.practicum.dto.EventFullDto;

import java.util.List;
import java.util.Optional;

public interface EventPublicService {

    List<EventShortDto> getAll(EventPublicFilter publicFilter, Integer from, Integer size);

    EventFullDto getById(Long eventId, Long userId);

    Optional<Event> findById(Long eventId);

    Event getById(Long eventId);

    EventFullDto getEventById(Long eventId);

    void updateConfirmedRequests(Long eventId, int size);

    List<EventShortDto> getRecommendationsForUser(long userId, long maxResults);

    void sendLike(Long userId, Long eventId);
}
