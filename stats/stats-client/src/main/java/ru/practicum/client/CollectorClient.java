package ru.practicum.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.collector.CollectorControllerGrpc;

import java.time.Instant;

@Slf4j
@Component
public class CollectorClient {

    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub client;

    public void collectUserAction(long userId, long eventId, ActionTypeProto actionType, Instant instant) {
        try {
            UserActionProto actionProto = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setTimestamp(toTimestamp(instant))
                    .setActionType(actionType)
                    .build();

            client.collectUserAction(actionProto);
        } catch (Exception e) {
            log.warn("Error collecting user action for user {}, event {}", userId, eventId, e);
        }
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
