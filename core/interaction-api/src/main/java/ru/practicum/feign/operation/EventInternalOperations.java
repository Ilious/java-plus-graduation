package ru.practicum.feign.operation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;

public interface EventInternalOperations {

    @GetMapping("/{eventId}")
    EventFullDto getById(@PathVariable Long eventId);

    @PutMapping("/{eventId}")
    void updateConfirmedRequests(@PathVariable Long eventId, @RequestParam int size);
}
