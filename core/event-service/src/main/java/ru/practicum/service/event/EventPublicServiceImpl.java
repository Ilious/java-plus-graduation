package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dao.event.EventSort;
import ru.practicum.dal.dto.event.EventPublicFilter;
import ru.practicum.dal.dto.event.EventShortDto;
import ru.practicum.dal.repository.event.EventRepository;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.EventState;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.StatRequest;
import ru.practicum.ewm.dto.ViewStatDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.client.CommentClient;
import ru.practicum.feign.client.UserClient;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.event.EventMapperHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPublicServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;

    private final StatClient statClient;

    private final UserClient userClient;

    private final EventMapper eventMapper;

    private final EventMapperHelper eventMapperHelper;

    private final CommentClient commentClient;

    @Override
    public List<EventShortDto> getAll(EventPublicFilter publicFilter, Integer from, Integer size,
                                      HttpServletRequest httpServletRequest) {
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

        hit(httpServletRequest);

        List<Event> events = eventRepository.findAll(specification, PageRequest.of(from / size, size)
                .withSort(sort))
                .getContent();
        return eventMapperHelper.toEventShortDtoList(events);
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = getById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("getById: Событие id = %d не опубликовано".formatted(eventId));
        }

        hit(httpServletRequest);

        StatRequest statsRequest = StatRequest.builder()
                .start(event.getPublishedOn())
                .end(LocalDateTime.now())
                .uris(List.of("/events/" + eventId))
                .unique(true)
                .build();

        List<ViewStatDto> stats = statClient.getStats(statsRequest);
        log.info("Метод getById, длина списка stats: {}", stats.size());

        Long views = stats.isEmpty() ? 0L : stats.getFirst().getHits();
        event.setViews(views);
        log.info("Метод getById, количество сохраняемых просмотров: {}", views);

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

    private void hit(HttpServletRequest httpServletRequest) {
        EndpointHit hitDtoRequest = new EndpointHit(
                null,
                "main-server",
                httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(),
                LocalDateTime.now()
        );
        statClient.saveHit(hitDtoRequest);
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
}
