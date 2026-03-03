package ru.practicum.service;


import ru.practicum.dal.dao.User;
import ru.practicum.dal.dto.NewUserRequest;
import ru.practicum.dal.dto.UserDto;
import ru.practicum.dto.UserShortDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDto create(NewUserRequest request);

    List<UserDto> getAll(List<Long> ids, int from, int size);

    void delete(Long id);

    Optional<User> findById(Long userId);

    User getUserById(Long userId);

    UserShortDto getById(Long userId);

    List<UserShortDto> getUsersByIds(List<Long> ids);
}
