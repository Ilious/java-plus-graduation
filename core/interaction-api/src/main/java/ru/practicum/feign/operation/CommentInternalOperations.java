package ru.practicum.feign.operation;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Validated
public interface CommentInternalOperations {

    @GetMapping("/{eventId}")
    Long getCountPublishedCommentsByEventId(@PathVariable("eventId") @Positive Long eventId);
}
