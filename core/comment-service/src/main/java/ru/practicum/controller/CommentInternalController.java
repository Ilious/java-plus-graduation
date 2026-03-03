package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.feign.operation.CommentInternalOperations;
import ru.practicum.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comments")
public class CommentInternalController implements CommentInternalOperations {

    private CommentService commentService;

    @Override
    public Long getCountPublishedCommentsByEventId(Long eventId) {
        return commentService.getCountPublishedCommentsByEventId(eventId);
    }
}
