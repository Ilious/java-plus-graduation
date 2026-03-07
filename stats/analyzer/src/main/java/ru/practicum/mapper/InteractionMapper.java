package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.practicum.dal.dao.Interaction;
import ru.practicum.ewm.stats.avro.ActionType;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InteractionMapper {

    @Mapping(source = "timestamp", target = "ts")
    @Mapping(qualifiedByName = "getActionScore", source = "actionType", target = "rating")
    Interaction toEntity(UserActionAvro action);

    @Named(value = "getActionScore")
    default double getActionScore(ActionType type) {
        return switch (type) {
            case VIEW -> 0.4f;
            case REGISTER -> 0.8f;
            case LIKE -> 1;
        };
    };
}
