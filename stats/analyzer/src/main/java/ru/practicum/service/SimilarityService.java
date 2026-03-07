package ru.practicum.service;

import ru.practicum.dal.dao.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Collection;
import java.util.List;

public interface SimilarityService {
    Similarity upsertSimilarity(EventSimilarityAvro eventAvro);

    List<Similarity> getSimilarEvents(long userId, long eventId, long maxResults);

    List<Similarity> getAllByIdIn(Collection<Long> interactionIds);
}
