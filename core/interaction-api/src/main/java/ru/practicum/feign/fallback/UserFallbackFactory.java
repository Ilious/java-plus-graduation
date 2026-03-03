package ru.practicum.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.UserShortDto;
import ru.practicum.exception.ServiceNotUpException;
import ru.practicum.feign.client.UserClient;

import java.util.List;

@Slf4j
@Component
public class UserFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("User link failed. Circuit opened due to: {}", cause.getMessage());

        return new UserClient() {
            @Override
            public UserShortDto getUserById(Long userId) {
                log.error("Cannot get user by id {}. UserClient is down", userId);
                throw new ServiceNotUpException("Get user failed", cause);
            }

            @Override
            public List<UserShortDto> getUsersByIds(List<Long> ids) {
                log.error("Cannot get users by ids {}. UserClient is down", ids);
                throw new ServiceNotUpException("Get users by ids failed", cause);
            }
        };
    }
}
