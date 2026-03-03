package ru.practicum.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.exception.ServiceNotUpException;
import ru.practicum.feign.client.CommentClient;

@Slf4j
@Component
public class CommentFallbackFactory implements FallbackFactory<CommentClient> {

    @Override
    public CommentClient create(Throwable cause) {
        log.error("Comment link failed. Circuit opened due to: {}", cause.getMessage());

        return new CommentClient() {
            @Override
            public Long getCountPublishedCommentsByEventId(Long eventId) {
                log.error("Cannot get count published comments by event id {}. CommentClient is down", eventId);
                throw new ServiceNotUpException("Get count published comments failed", cause);
            }
        };
    }
}
