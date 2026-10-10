package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.dao.Interaction;
import ru.practicum.dal.dao.RecommendedEventProjection;
import ru.practicum.dal.dao.Similarity;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;
import ru.practicum.mapper.RecommendationMapper;
import ru.practicum.service.InteractionService;
import ru.practicum.service.RecommendationsService;
import ru.practicum.service.SimilarityService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationsService {

    private final InteractionService interactionService;

    private final SimilarityService similarityService;

    private final RecommendationMapper recommendationMapper;

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        List<RecommendedEventProjection> interactions = interactionService.getSumWeightsByEventId(eventIds);

        return interactions.stream()
                .filter(it -> it.getEventId() != null)
                .map(it -> RecommendedEventProto.newBuilder()
                        .setEventId(it.getEventId())
                        .setScore(it.getScore() == null ? 0.0 : it.getScore())
                        .build());
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<Similarity> similarEvents = similarityService.getSimilarEvents(request.getUserId(),
                request.getEventId(), request.getMaxResults());


        Set<Long> similarEventIds = similarEvents.stream()
                .flatMap(similarity -> Stream.of(similarity.getEventId1(), similarity.getEventId2()))
                .collect(Collectors.toSet());
        List<Long> eventIdsUserInteracted = interactionService.getAllIdsById(similarEventIds);

        return similarEvents.stream()
                .flatMap(it -> Stream.of(
                        recommendationMapper.toProto(it.getEventId1(), it.getSimilarity()),
                        recommendationMapper.toProto(it.getEventId2(), it.getSimilarity())
                ))
                .filter(it -> !eventIdsUserInteracted.contains(it.getEventId()))
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResults());

    }

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        List<Interaction> interactions = interactionService.getAllByUserId(request.getUserId());
        if (interactions.isEmpty())
            return Stream.empty();

        Map<Long, Double> ratedInteractions = getInteractionSortedByDays(interactions);

        Set<Long> interactionIds = interactions.stream()
                .map(Interaction::getId)
                .collect(Collectors.toSet());

        List<Similarity> similarEvents = similarityService.getAllByIdIn(interactionIds);

        return similarEvents.stream()
                .flatMap(similarity -> Stream.of(
                                recommendationMapper.toProto(similarity.getEventId1(),
                                        similarity.getSimilarity() * ratedInteractions
                                                .getOrDefault(similarity.getEventId1(), 1.0)),
                                recommendationMapper.toProto(similarity.getEventId2(),
                                        similarity.getSimilarity() * ratedInteractions
                                                .getOrDefault(similarity.getEventId2(), 1.0))
                        )
                )
                .filter(it -> !interactionIds.contains(it.getEventId()))
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResults());
    }

    private Map<Long, Double> getInteractionSortedByDays(List<Interaction> interactions) {
        Instant now = Instant.now();
        return interactions.stream()
                .collect(Collectors.toMap(Interaction::getEventId, it -> {
                    long daysSince = ChronoUnit.DAYS.between(now, it.getTs());
                    return it.getRating() * Math.exp(-0.1 * daysSince);
                }));
    }
}
