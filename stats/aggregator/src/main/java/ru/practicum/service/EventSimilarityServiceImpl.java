package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionType;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final Map<Long, Double> eventWeightsSum = new ConcurrentHashMap<>();

    private final Map<Long, Map<Long, Double>> userActionsWeight = new ConcurrentHashMap<>();

    // action -> action : minSum
    private final Map<Long, Map<Long, Double>> minWeightSum = new ConcurrentHashMap<>();

    @Override
    public List<EventSimilarityAvro> calculateWeightSums(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();

        Double oldWeight = getWeight(userId, eventId);
        Double newWeight = getActionWeight(action.getActionType());

        if (oldWeight >= newWeight) {
            log.debug("Weight didn't update for userId {}, eventId {}, oldWeight {}, newWeight {}",
                    userId, eventId, oldWeight, newWeight);
            return Collections.emptyList();
        }

        double deltaWeight = newWeight - oldWeight;
        eventWeightsSum.merge(eventId, deltaWeight, Double::sum);

        Map<Long, Double> userHistory = userActionsWeight.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        List<EventSimilarityAvro> results = new ArrayList<>();

        for (Map.Entry<Long, Double> pair : userHistory.entrySet()) {
            Long otherEventId = pair.getKey();
            if (otherEventId == eventId) continue;

            double otherEventWeight = pair.getValue();

            double oldMin = Math.min(oldWeight, otherEventWeight);
            double newMin = Math.min(newWeight, otherEventWeight);
            double deltaMin = newMin - oldMin;

            double updatedMinSum = updateMinWeightSum(eventId, otherEventId, deltaMin);

            double score = calculateSimilarityScore(eventId, otherEventId, updatedMinSum);

            if (score > 0)
                results.add(toMessage(eventId, otherEventId, score, action.getTimestamp()));
        }

        userHistory.put(eventId, newWeight);

        return results;
    }

    private double updateMinWeightSum(long eventId, Long otherEventId, double delta) {
        long first = Math.min(eventId, otherEventId);
        long second = Math.max(eventId, otherEventId);

        return minWeightSum
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .merge(second, delta, Double::sum);
    }

    private double calculateSimilarityScore(long idA, long idB, double currentMinSum) {
        double sumA = eventWeightsSum.getOrDefault(idA, 0.0);
        double sumB = eventWeightsSum.getOrDefault(idB, 0.0);

        if (sumA <= 0 || sumB <= 0) return 0.0;

        return currentMinSum / Math.sqrt(sumA * sumB);
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
        Map<Long, Double> userMap = userActionsWeight.get(userId);
        return (userMap != null) ? userMap.getOrDefault(eventId, 0.0) : 0.0;
    }

    private double getActionWeight(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
