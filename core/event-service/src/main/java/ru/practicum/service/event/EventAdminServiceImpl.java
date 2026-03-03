package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dao.event.StateAction;
import ru.practicum.dal.dto.event.EventAdminFilter;
import ru.practicum.dal.dto.event.UpdateEventAdminRequest;
import ru.practicum.dal.repository.category.CategoryRepository;
import ru.practicum.dal.repository.event.EventRepository;
import ru.practicum.dal.repository.event.LocationRepository;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.event.EventMapperHelper;
import ru.practicum.mapper.event.LocationMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventAdminServiceImpl implements EventAdminService {

    private final EventRepository eventRepository;

    private final CategoryRepository categoryRepository;

    private final LocationRepository locationRepository;

    private final EventMapper eventMapper;

    private final LocationMapper locationMapper;

    private final EventMapperHelper eventMapperHelper;

    @Override
    public List<EventFullDto> getAll(EventAdminFilter adminFilter, Integer from, Integer size) {

        Specification<Event> specification = DbSpecification.getAdminSpecification(
                adminFilter.getUserIds(),
                adminFilter.getStates(),
                adminFilter.getCategoryIds(),
                adminFilter.getRangeStart(),
                adminFilter.getRangeEnd());

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();
        List<EventFullDto> eventFullDtoList = eventMapperHelper.toEventFullDtoList(events);

        return eventFullDtoList.stream()
                .sorted(Comparator.comparingLong(EventFullDto::getId).reversed())
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventAdminRequest request, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));

        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory()).orElseThrow(() ->
                    new NotFoundException("Категория с id = %d не найдена".formatted(request.getCategory()))));
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
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            setEventDate(event, String.valueOf(request.getEventDate()));
        }
        setState(event, request);

        Event eventSaved = eventRepository.save(event);

        return eventMapperHelper.toEventFullDtoWithInitiator(eventSaved);
    }

    private void setEventDate(Event event, String date) {
        if (date != null) {
            LocalDateTime eventDateTime;

            try {
                eventDateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                try {
                    eventDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException ex) {
                    throw new ValidationException("Неверный формат даты. Используйте yyyy-MM-ddTHH:mm:ss или yyyy-MM-dd HH:mm:ss");
                }
            }

            if (eventDateTime.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Указанная дата уже наступила");
            }
            event.setEventDate(eventDateTime);
        }
    }

    private void setState(Event event, UpdateEventAdminRequest request) {
        if (request.getStateAction() != null) {
            if (request.getStateAction() == StateAction.PUBLISH_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Событие id = %d уже опубликовано".formatted(event.getId()));
                } else if (event.getState() == EventState.REJECT) {
                    throw new ConflictException("Событие id = %d отменено".formatted(event.getId()));
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (request.getStateAction() == StateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отменить опубликованное событие");
                }
                event.setState(EventState.REJECT);
            }
        }
    }
}
