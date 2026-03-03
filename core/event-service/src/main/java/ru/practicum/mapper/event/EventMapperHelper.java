package ru.practicum.mapper.event;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dto.event.EventShortDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.feign.client.UserClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventMapperHelper {

    private final UserClient userClient;

    private final EventMapper eventMapper;

    public List<EventFullDto> toEventFullDtoList(List<Event> events) {
        if (events.isEmpty()) return Collections.emptyList();

        Map<Long, UserShortDto> userMap = fetchUserMap(events);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setInitiator(userMap.get(event.getInitiatorId()));
                    return dto;
                })
                .toList();
    }

    public List<EventShortDto> toEventShortDtoList(List<Event> events) {
        if (events.isEmpty()) return Collections.emptyList();

        Map<Long, UserShortDto> userMap = fetchUserMap(events);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setInitiator(userMap.get(event.getInitiatorId()));
                    return dto;
                })
                .toList();
    }

    public EventFullDto toEventFullDtoWithInitiator(Event event) {
        EventFullDto dto = eventMapper.toEventFullDto(event);
        try {
            dto.setInitiator(userClient.getUserById(event.getInitiatorId()));
        } catch (FeignException e) {
            log.error("Ошибка получения пользователя с id {} для события {}", event.getInitiatorId(), event.getId(), e);
        }
        return dto;
    }

    private Map<Long, UserShortDto> fetchUserMap(List<Event> events) {
        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        return userClient.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
    }
}
