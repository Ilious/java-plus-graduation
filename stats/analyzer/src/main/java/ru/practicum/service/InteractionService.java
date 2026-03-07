package ru.practicum.service;

import ru.practicum.dal.dao.Interaction;
import ru.practicum.dal.dao.RecommendedEventProjection;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Collection;
import java.util.List;

public interface InteractionService {
    List<Long> getAllIdsById(Collection<Long> eventIds);

    List<Interaction> getAllByUserId(Long userId);

    List<RecommendedEventProjection> getSumWeightsByEventId(Collection<Long> ids);

    Interaction upsertInteraction(UserActionAvro action);
}
