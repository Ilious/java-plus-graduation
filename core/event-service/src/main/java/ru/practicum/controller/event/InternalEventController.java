package ru.practicum.controller.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EventFullDto;
import ru.practicum.feign.operation.EventInternalOperations;
import ru.practicum.service.event.EventPublicService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController implements EventInternalOperations {

    private final EventPublicService eventService;

    @Override
    public EventFullDto getById(Long eventId) {
        return eventService.getEventById(eventId);
    }

    @Override
    public void updateConfirmedRequests(Long eventId, int size) {
        eventService.updateConfirmedRequests(eventId, size);
    }
}
