package ru.practicum.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.exception.ServiceNotUpException;
import ru.practicum.feign.client.RequestClient;

@Slf4j
@Component
public class RequestFallbackFactory implements FallbackFactory<RequestClient> {

    @Override
    public RequestClient create(Throwable cause) {
        log.error("Request link failed. Circuit opened due to: {}", cause.getMessage());

        return new RequestClient() {
            @Override
            public boolean existsByUserIdAndEventId(Long userId, Long eventId) {
                log.error("Cannot verify exists request for user {}, event {}. RequestClient is down", userId, eventId);
                throw new ServiceNotUpException("Verification existence request by user and event failed", cause);
            }
        };
    }
}
