package ru.practicum.stats.collector.service.handler.base;


import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;

public interface ActionHandler {

    ActionTypeProto getMessageType();

    UserActionAvro toMessage(UserActionProto userAction);

    void handle(UserActionProto action);
}
