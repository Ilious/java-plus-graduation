package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dal.dao.User;
import ru.practicum.dal.dto.NewUserRequest;
import ru.practicum.dal.dto.UserDto;
import ru.practicum.dto.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    User toEntity(UserDto userDto);

    User toEntity(NewUserRequest newUserRequest);

    UserShortDto toShortDto(User user);
}
