package ru.practicum.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.stats.collector.service.handler.base.ActionHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final Map<ActionTypeProto, ActionHandler> handlers;

    public UserActionController(Set<ActionHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(ActionHandler::getMessageType, Function.identity()));
    }

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (!handlers.containsKey(request.getActionType())) {
                String message = String.format("Can't find handler for the type: %s", request.getActionType());
                log.warn(message);
                throw new IllegalArgumentException(message);
            }

            ActionHandler handler = handlers.get(request.getActionType());

            handler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error handling UserActionProto request", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
