package ru.practicum.feign.operation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.UserShortDto;

import java.util.List;

@Validated
public interface UserInternalOperations {

    @GetMapping("/{userId}")
    UserShortDto getUserById(@PathVariable @Positive Long userId);

    @GetMapping
    List<UserShortDto> getUsersByIds(@RequestParam @NotNull List<Long> ids);
}
