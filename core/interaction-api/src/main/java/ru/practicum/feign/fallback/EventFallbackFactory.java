package ru.practicum.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;
import ru.practicum.exception.ServiceNotUpException;
import ru.practicum.feign.client.EventClient;

@Slf4j
@Component
public class EventFallbackFactory implements FallbackFactory<EventClient> {

    @Override
    public EventClient create(Throwable cause) {
        log.error("Event link failed. Circuit opened due to: {}", cause.getMessage());

        return new EventClient() {
            @Override
            public EventFullDto getById(Long eventId) {
                log.error("Cannot get event by id {}. EventClient is down", eventId);
                throw new ServiceNotUpException("Get event failed", cause);
            }

            @Override
            public void updateConfirmedRequests(Long eventId, int size) {
                log.error("Cannot update confirmed request by event id {}. EventClient is down", eventId);
                throw new ServiceNotUpException("Update confirmed request failed", cause);
            }
        };
    }
}
