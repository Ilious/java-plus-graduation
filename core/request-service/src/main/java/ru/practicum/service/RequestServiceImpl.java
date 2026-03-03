package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.dao.ParticipationRequest;
import ru.practicum.dal.repository.RequestRepository;
import ru.practicum.dto.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.client.EventClient;
import ru.practicum.feign.client.UserClient;
import ru.practicum.mapper.RequestMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;

    private final UserClient userClient;

    private final EventClient eventClient;

    private final RequestMapper requestMapper;

    @Override
    public Optional<ParticipationRequest> findById(Long requestId) {
        return requestRepository.findById(requestId);
    }

    @Override
    public ParticipationRequest getById(Long requestId) {
        return findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с id = %d не найден".formatted(requestId)));
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        checkExistsUser(userId);

        EventFullDto event = eventClient.getById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(("Инициатор события(userId = %d) не может подать заявку на участие" +
                    " в собственном событии(eventId = %d)").formatted(userId, eventId));
        }

        if (!EventState.PUBLISHED.name().equals(event.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии(eventId = %d)"
                    .formatted(eventId));
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException(("Заявка на участие в этом событии(eventId = %d) уже существует" +
                    " от пользователя(userId = %d)").formatted(eventId, userId));
        }

        if (event.getParticipantLimit() > 0) {
            Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников для этого события");
            }
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .eventId(event.getId())
                .requesterId(userId)
                .status(RequestStatus.PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);

            int newConfirmed = event.getConfirmedRequests() + 1;
            eventClient.updateConfirmedRequests(eventId, newConfirmed);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        checkExistsUser(userId);

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = getById(requestId);

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException("Запрос с id = %d не найден для пользователя с userId = %d"
                    .formatted(userId, requestId));
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);

        return requestMapper.toDto(updatedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        checkExistsUser(userId);

        EventFullDto event = eventClient.getById(eventId);
        checkInitiator(userId, event);

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequest(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                        Long eventId) {
        EventFullDto event = checkUpdateEvent(userId, eventId);
        EventState eventState = EventState.valueOf(event.getState());

        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        if (eventState != EventState.PUBLISHED) {
            throw new ConflictException("Событие id = %d не опубликовано".formatted(eventId));
        }
        if (event.getConfirmedRequests() != null) {
            if (RequestStatus.CONFIRMED.equals(requestDto.getStatus())
                    && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит заявок");
            }
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(requestDto.getRequestIds());
        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        requests.forEach(request -> {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус заявки id=%d не в состоянии ожидания".formatted(request.getId()));
            }

            if (requestDto.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(request);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(request);
                }
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        });

        requestRepository.saveAll(requests);

        eventClient.updateConfirmedRequests(eventId, event.getConfirmedRequests());

        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream().map(requestMapper::toDto).toList(),
                rejectedRequests.stream().map(requestMapper::toDto).toList()
        );
    }

    private EventFullDto checkUpdateEvent(Long userId, Long eventId) {
        EventFullDto event = eventClient.getById(eventId);
        checkExistsUser(userId);
        checkInitiator(userId, event);

        return event;
    }

    private void checkInitiator(Long userId, EventFullDto event) {
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Пользователь id = %d не является создателем события id = %d"
                    .formatted(userId, event.getId()));
        }
    }

    private void checkExistsUser(Long userId) {
        UserShortDto user = userClient.getUserById(userId);
        log.info("Получен пользователь {}", user);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = %d не найден".formatted(userId));
        }
    }
}
