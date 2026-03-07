package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionType;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final Map<Long, Double> eventWeightsSum = new HashMap<>();

    private final Map<Long, Map<Long, Double>> userActionsWeight = new HashMap<>();

    // action -> action : minSum
    private final Map<Long, Map<Long, Double>> minWeightSum = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> calculateWeightSums(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();

        Double oldWeight = getWeight(userId, eventId);
        Double newWeight = getActionWeight(action.getActionType());

        if (oldWeight > newWeight) {
            log.debug("Weight didn't update for userId {}, eventId {}, oldWeight {}, newWeight {}",
                    userId, eventId, oldWeight, newWeight);
            return Collections.emptyList();
        }

        updateStorageData(userId, eventId, oldWeight, newWeight);

        return minWeightSum.get(action.getEventId()).entrySet()
                .stream()
                .map(entry ->
                        toMessage(eventId, entry.getKey(), entry.getValue(), action.getTimestamp()))
                .toList();
    }

    private void updateStorageData(long userId, long eventId, Double oldWeight, Double newWeight) {
        userActionsWeight.get(userId)
                .put(eventId, newWeight);

        eventWeightsSum.compute(eventId,
                (k, oldEventWeight) -> (oldEventWeight == null ? 0.0 : oldEventWeight) +
                        newWeight - oldWeight);

        for (Map.Entry<Long, Double> userActionEntry: userActionsWeight.get(userId).entrySet()) {
            if (userActionEntry.getKey() == eventId || userActionEntry.getValue() == 0)
                continue;

            recalculateEventSimilarity(userId, eventId, userActionEntry.getKey(), newWeight);
        }
    }

    private void recalculateEventSimilarity(long userId, long eventIdUpd, long eventId, double newWeight) {
        Map<Long, Double> userActions = userActionsWeight.get(userId);

        double minOldWeight = Math.min(userActions.get(eventIdUpd), userActions.get(eventId));
        double minNewWeight = Math.min(newWeight, userActions.get(eventId));

        double deltaMin = minNewWeight - minOldWeight;

        long first = Math.min(eventIdUpd, eventId);
        long second = Math.max(eventIdUpd, eventId);

        put(first, second, get(first, second) + deltaMin);
    }

    private void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private EventSimilarityAvro toMessage(long eventId1, long eventId2, Double sum,
                                                                      Instant timestamp) {
        long first = Math.min(eventId1, eventId2);
        long second = Math.max(eventId1, eventId2);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(sum)
                .setTimestamp(timestamp)
                .build();
    }

    private Double getWeight(long userId, long eventId) {
        return userActionsWeight
                .computeIfAbsent(userId, e -> new HashMap<>())
                .getOrDefault(eventId, 0.0);
    }

    private double getActionWeight(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1;
        };
    }
}
