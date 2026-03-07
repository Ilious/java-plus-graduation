package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dal.dao.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SimilarityMapper {

    @Mapping(source = "timestamp", target = "ts")
    @Mapping(source = "eventA", target = "eventId1")
    @Mapping(source = "eventB", target = "eventId2")
    @Mapping(source = "score", target = "similarity")
    Similarity toEntity(EventSimilarityAvro similarity);
}
