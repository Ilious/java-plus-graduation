package ru.practicum.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.UserShortDto;
import ru.practicum.feign.operation.UserInternalOperations;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class UserInternalController implements UserInternalOperations {

    private final UserService userService;

    @Override
    public UserShortDto getUserById(Long userId) {
        return userService.getById(userId);
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        return userService.getUsersByIds(ids);
    }
}
