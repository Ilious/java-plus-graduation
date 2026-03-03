package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.feign.fallback.UserFallbackFactory;
import ru.practicum.feign.operation.UserInternalOperations;

@FeignClient(name = "user-service", path = "/internal/users", fallbackFactory = UserFallbackFactory.class)
public interface UserClient extends UserInternalOperations {
}
