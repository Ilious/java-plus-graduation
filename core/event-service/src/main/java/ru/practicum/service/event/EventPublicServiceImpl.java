package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.CollectorClient;
import ru.practicum.client.RecommendationClient;
import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dao.event.EventSort;
import ru.practicum.dal.dto.event.EventPublicFilter;
import ru.practicum.dal.dto.event.EventShortDto;
import ru.practicum.dal.repository.event.EventRepository;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.client.CommentClient;
import ru.practicum.feign.client.RequestClient;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.event.EventMapperHelper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPublicServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;

    private final CollectorClient collectorClient;

    private final RecommendationClient recommendationClient;

    private final RequestClient requestClient;

    private final EventMapper eventMapper;

    private final EventMapperHelper eventMapperHelper;

    private final CommentClient commentClient;

    @Override
    public List<EventShortDto> getAll(EventPublicFilter publicFilter, Integer from, Integer size) {
        publicFilter.validateDates();
        Specification<Event> specification = DbSpecification.getPublicSpecification(
                publicFilter.getText(),
                publicFilter.getCategoryIds(),
                publicFilter.getPaid(),
                publicFilter.getRangeStart(),
                publicFilter.getRangeEnd(),
                publicFilter.getOnlyAvailable());

        Sort sort = Optional.ofNullable(publicFilter.getSort())
                .map(s -> Sort.by(Sort.Direction.DESC, s == EventSort.EVENT_DATE ? "eventDate" : "views"))
                .orElse(Sort.unsorted());

        List<Event> events = eventRepository.findAll(specification, PageRequest.of(from / size, size)
                        .withSort(sort))
                .getContent();
        return eventMapperHelper.toEventShortDtoList(events);
    }

    @Override
    public EventFullDto getById(Long eventId, Long userId) {
        Event event = getById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("getById: Событие id = %d не опубликовано".formatted(eventId));
        }

        sendViewAction(userId, eventId, Instant.now());

        Double eventRating = getEventRating(eventId);
        event.setRating(eventRating);
        log.debug("Метод getById, рейтинг: {}", eventRating);

        EventFullDto eventFullDto = eventMapperHelper.toEventFullDtoWithInitiator(event);
        Long commentsCount = commentClient.getCountPublishedCommentsByEventId(eventId);
        eventFullDto.setCommentsCount(commentsCount);

        return eventFullDto;
    }

    @Override
    public Optional<Event> findById(Long eventId) {
        return eventRepository.findById(eventId);
    }

    @Override
    public Event getById(Long eventId) {
        return findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
    }

    private void sendViewAction(long userId, long eventId, Instant instant) {
        try {
            collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW, instant);
        } catch (Exception e) {
            log.warn("Error collecting user action VIEW, {}, {}", userId, eventId, e);
        }
    }

    private Double getEventRating(long eventId) {
        try {
            return recommendationClient.getInteractionsCount(List.of(eventId))
                    .findFirst()
                    .map(RecommendedEventProto::getScore)
                    .orElse(0.0);

        } catch (Exception e) {
            log.warn("Error getting rating for event {}", eventId, e);
            return 0.0;
        }
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = getById(eventId);
        return eventMapperHelper.toEventFullDtoWithInitiator(event);
    }

    @Override
    @Transactional
    public void updateConfirmedRequests(Long eventId, int size) {
        Event event = getById(eventId);
        event.setConfirmedRequests(size);
        eventRepository.save(event);
    }

    @Override
    public List<EventShortDto> getRecommendationsForUser(long userId, long maxResults) {
        return recommendationClient
                .getRecommendationsForUser(userId, maxResults).map(eventProto -> {
                    long event = eventProto.getEventId();
                    EventShortDto eventShortDto = eventMapper.toEventShortDto(getById(event));
                    eventShortDto.setRating(eventProto.getScore());
                    return eventShortDto;
                })
                .toList();
    }

    @Override
    public void sendLike(Long userId, Long eventId) {
        if (!requestClient.existsByUserIdAndEventId(userId, eventId))
            throw new ConflictException("Пользователь %s не посетил мероприятие %s".formatted(userId, eventId));

        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
    }
}
