package ru.practicum.service.handler.base;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.stats.avro.ActionType;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.service.KafkaActionProducer;

import java.time.Instant;

@RequiredArgsConstructor
public abstract class BaseActionHandler implements ActionHandler {

    private final KafkaActionProducer producer;

    @Override
    public abstract ActionTypeProto getMessageType();

    protected ActionType getAvroType() {
        try {
            return ActionType.valueOf(getMessageType().name()
                    .replace("ACTION_", "")
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Unmapped ActionType: %s", getMessageType()));
        }
    }

    private static Instant toInstant(Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    @Override
    public UserActionAvro toMessage(UserActionProto userAction) {
        Instant valueInstant = toInstant(userAction.getTimestamp());

        return UserActionAvro.newBuilder()
                .setUserId(userAction.getUserId())
                .setEventId(userAction.getEventId())
                .setActionType(getAvroType())
                .setTimestamp(valueInstant)
                .build();
    }

    @Override
    public void handle(UserActionProto action) {
        UserActionAvro message = toMessage(action);

        producer.send(action.getUserId(), toInstant(action.getTimestamp()), message);
    }
}
