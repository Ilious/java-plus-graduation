package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.feign.fallback.RequestFallbackFactory;
import ru.practicum.feign.operation.RequestInternalOperations;

@FeignClient(name = "request-service", path = "/internal/requests", fallbackFactory = RequestFallbackFactory.class)
public interface RequestClient extends RequestInternalOperations {
}
