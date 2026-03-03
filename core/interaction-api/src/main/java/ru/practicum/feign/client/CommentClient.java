package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.feign.fallback.CommentFallbackFactory;
import ru.practicum.feign.operation.CommentInternalOperations;

@FeignClient(name = "comment-service", path = "/internal/comments", fallbackFactory = CommentFallbackFactory.class)
public interface CommentClient extends CommentInternalOperations {
}
