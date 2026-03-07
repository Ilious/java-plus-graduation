package ru.practicum.service.handler;

import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.service.KafkaActionProducer;
import ru.practicum.service.handler.base.BaseActionHandler;

@Component
public class LikeHandler extends BaseActionHandler {

    public LikeHandler(KafkaActionProducer producer) {
        super(producer);
    }

    @Override
    public ActionTypeProto getMessageType() {
        return ActionTypeProto.ACTION_LIKE;
    }
}
