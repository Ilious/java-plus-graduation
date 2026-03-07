package ru.practicum.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.analyzer.RecommendationControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class RecommendationClient {

    @GrpcClient("analyzer")
    private RecommendationControllerGrpc.RecommendationControllerBlockingStub client;

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);

            return asStream(iterator);
        } catch (Exception e) {
            log.warn("Error getting similar events for user {}, event {}", userId, eventId, e);

            throw new StatusRuntimeException(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .withCause(e)
            );
        }
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, long maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);

            return asStream(iterator);
        } catch (Exception e) {
            log.warn("Error getting recommendations for user {}", userId, e);

            throw new StatusRuntimeException(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .withCause(e)
            );
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();

            Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);

            return asStream(iterator);
        } catch (Exception e) {
            log.warn("Error getting interactions count for events {}", eventIds, e);

            throw new StatusRuntimeException(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .withCause(e)
            );
        }
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}