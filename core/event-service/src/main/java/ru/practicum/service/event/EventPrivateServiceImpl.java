package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.dao.category.Category;
import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dao.event.Location;
import ru.practicum.dal.dao.event.StateAction;
import ru.practicum.dal.dto.event.EventShortDto;
import ru.practicum.dal.dto.event.NewEventDto;
import ru.practicum.dal.dto.event.UpdateEventUserRequest;
import ru.practicum.dal.repository.event.EventRepository;
import ru.practicum.dal.repository.event.LocationRepository;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.client.UserClient;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.event.EventMapperHelper;
import ru.practicum.mapper.event.LocationMapper;
import ru.practicum.service.category.CategoryService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPrivateServiceImpl implements EventPrivateService {

    private final EventRepository eventRepository;

    private final CategoryService categoryService;

    private final LocationRepository locationRepository;

    private final EventMapper eventMapper;

    private final EventMapperHelper eventMapperHelper;

    private final LocationMapper locationMapper;

    private final UserClient userClient;

    private final EventPublicService eventPublicService;

    @Override
    public List<EventShortDto> getAll(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();

        return eventMapperHelper.toEventShortDtoList(events);
    }

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Указана дата начала события в прошлом");
        }
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        UserShortDto user = userClient.getUserById(userId);
        Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));

        Event event = eventMapper.toEvent(newEventDto, category, user, location);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        event.setConfirmedRequests(0);
        event.setState(EventState.PENDING);
        Event savedEvent = eventRepository.save(event);

        return eventMapperHelper.toEventFullDtoWithInitiator(savedEvent);
    }

    @Override
    public EventFullDto getByInitiatorId(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
        return eventMapperHelper.toEventFullDtoWithInitiator(event);
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId) {
        Event event = checkUpdateEvent(userId, eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Событие id = %d не отменено и не в состоянии ожидания.".formatted(eventId));
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Время события указано раньше, чем через два часа от текущего момента");
        }
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(request.getCategory().getId()));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(locationMapper.toLocation(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ValidationException("Нельзя установить отрицательное значение лимита");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
        if (request.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }
        if (request.getEventDate() != null) {
            setEventDate(event, String.valueOf(request.getEventDate()));
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case REJECT_EVENT -> event.setState(EventState.REJECT);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case PUBLISH_EVENT -> event.setState(EventState.PUBLISHED);
            }
        }

        Event savedEvent = eventRepository.save(event);

        return eventMapperHelper.toEventFullDtoWithInitiator(savedEvent);
    }

    private Event checkUpdateEvent(Long userId, Long eventId) {
        Event event = eventPublicService.getById(eventId);
        userClient.getUserById(userId);
        checkInitiator(userId, event);

        return event;
    }

    private void setEventDate(Event event, String date) {
        if (date != null) {
            String normalizedDate = date.replace('T', ' ');
            LocalDateTime eventDateTime = LocalDateTime.parse(normalizedDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if (eventDateTime.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Указанная дата уже наступила");
            }
            event.setEventDate(eventDateTime);
        }
    }

    private void checkInitiator(Long userId, Event event) {
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new ValidationException("Пользователь id = %d не является создателем события id = %d"
                    .formatted(userId, event.getId()));
        }
    }
}
