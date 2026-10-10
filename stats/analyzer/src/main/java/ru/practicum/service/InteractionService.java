package ru.practicum.service;

import ru.practicum.dal.dao.Interaction;
import ru.practicum.dal.dao.RecommendedEventProjection;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;
import java.util.Set;

public interface InteractionService {

    void upsertInteraction(UserActionAvro action);

    List<RecommendedEventProjection> getSumWeightsByEventId(Set<Long> eventIds);

    List<Long> getAllIdsById(Set<Long> similarEventIds);

    List<Interaction> getAllByUserId(long userId);
}
