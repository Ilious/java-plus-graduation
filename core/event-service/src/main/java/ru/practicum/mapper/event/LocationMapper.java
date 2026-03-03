package ru.practicum.mapper.event;

import org.mapstruct.Mapper;
import ru.practicum.dal.dao.event.Location;
import ru.practicum.dto.LocationDto;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);
}
