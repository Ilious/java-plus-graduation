package ru.practicum.service;


import ru.practicum.dal.dao.ParticipationRequest;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Optional;

public interface RequestService {
    Optional<ParticipationRequest> findById(Long requestId);

    ParticipationRequest getById(Long requestId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequest(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                 Long eventId);

    boolean existsByUserAndEvent(Long userId, Long eventId);
}
