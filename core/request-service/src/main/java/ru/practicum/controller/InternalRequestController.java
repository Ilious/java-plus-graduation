package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.feign.operation.RequestInternalOperations;
import ru.practicum.service.RequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
public class InternalRequestController implements RequestInternalOperations {

    private final RequestService requestService;

    @Override
    public boolean existsByUserIdAndEventId(Long userId, Long eventId) {
        return requestService.existsByUserAndEvent(userId, eventId);
    }
}
