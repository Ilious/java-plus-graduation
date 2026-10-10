package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dal.dao.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SimilarityMapper {

    @Mapping(source = "similarity.timestamp", target = "ts")
    @Mapping(source = "id1", target = "eventId1")
    @Mapping(source = "id2", target = "eventId2")
    @Mapping(source = "similarity.score", target = "similarity")
    Similarity toEntity(EventSimilarityAvro similarity, Long id1, Long id2);
}
