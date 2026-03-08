package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecommendationMapper {

    @Mapping(source = "similarity", target = "score")
    @Mapping(source = "eventId", target = "eventId")
    RecommendedEventProto toProto(long eventId, double similarity);
}
