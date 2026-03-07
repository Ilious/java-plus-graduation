package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.analyzer.RecommendationControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;
import ru.practicum.service.RecommendationService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationController extends RecommendationControllerGrpc.RecommendationControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Getting interactions count for eventIds {}", request.getEventIdList());
            List<RecommendedEventProto> interactionsCount = recommendationService.getInteractionsCount(request);

            interactionsCount.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Error happened during getting interactions count for events {}", request.getEventIdList());

            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Getting similar events for event {}, user {}", request.getEventId(), request.getUserId());
            List<RecommendedEventProto> similarEvents = recommendationService.getSimilarEvents(request);

            similarEvents.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Error happened during getting similar events for user {}, user {}",
                    request.getEventId(), request.getUserId());

            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Getting recommendations for user {}", request.getUserId());
            List<RecommendedEventProto> recommendations = recommendationService.getRecommendationsForUser(request);

            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Error happened during getting recommendations for user {}", request.getUserId());

            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
