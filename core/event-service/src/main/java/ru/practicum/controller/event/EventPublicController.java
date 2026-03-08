package ru.practicum.controller.event;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dal.dao.event.EventSort;
import ru.practicum.dal.dto.event.EventPublicFilter;
import ru.practicum.dal.dto.event.EventShortDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.service.event.EventPublicService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {

    private final EventPublicService eventPublicService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
                                         @RequestParam(required = false) List<Long> categoryIds,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         LocalDateTime rangeStart,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос на получение событий с фильтрацией");
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        EventPublicFilter filter = EventPublicFilter.builder()
                .text(text)
                .categoryIds(categoryIds)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort != null ? EventSort.valueOf(sort.toUpperCase()) : null)
                .build();
        return eventPublicService.getAll(filter, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto geEventById(@PathVariable @Positive Long id,
                                    @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Запрос на получение события id = {}", id);
        return eventPublicService.getById(id, userId);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId,
                                                  @RequestParam(value = "max-result", required = false,
                                                          defaultValue = "10L") Long maxResults) {
        return eventPublicService.getRecommendationsForUser(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    public void sendLike(@RequestHeader("X-EWM-USER-ID") Long userId,
                         @PathVariable @Positive Long eventId) {
        eventPublicService.sendLike(userId, eventId);
    }
}