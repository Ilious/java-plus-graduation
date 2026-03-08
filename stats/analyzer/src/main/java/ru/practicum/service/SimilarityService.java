package ru.practicum.service;

import ru.practicum.dal.dao.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;
import java.util.Set;

public interface SimilarityService {

    void upsertSimilarity(EventSimilarityAvro eventAvro);

    List<Similarity> getSimilarEvents(long userId, long eventId, long maxResults);

    List<Similarity> getAllByIdIn(Set<Long> interactionIds);
}
