package ru.practicum.feign.operation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;

@Validated
public interface EventInternalOperations {

    @GetMapping("/{eventId}")
    EventFullDto getById(@PathVariable @Positive Long eventId);

    @PutMapping("/{eventId}")
    void updateConfirmedRequests(@PathVariable @Positive Long eventId, @RequestParam @PositiveOrZero int size);
}
