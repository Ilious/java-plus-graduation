package ru.practicum.feign.operation;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Validated
public interface RequestInternalOperations {

    @GetMapping("/users/{userId}/events/{eventId}")
    boolean existsByUserIdAndEventId(@PathVariable("userId") @Positive Long userId,
                                     @PathVariable("eventId") @Positive Long eventId);
}
