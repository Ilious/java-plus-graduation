package ru.practicum.feign.operation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface CommentInternalOperations {

    @GetMapping("/{eventId}")
    Long getCountPublishedCommentsByEventId(@PathVariable Long eventId);
}
