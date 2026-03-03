package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.feign.fallback.EventFallbackFactory;
import ru.practicum.feign.operation.EventInternalOperations;

@FeignClient(name = "event-service", path = "/internal/events", fallbackFactory = EventFallbackFactory.class)
public interface EventClient extends EventInternalOperations {
}
