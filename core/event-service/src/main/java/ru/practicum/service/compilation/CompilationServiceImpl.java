package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.dao.compilation.Compilation;
import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dto.compilation.CompilationDto;
import ru.practicum.dal.dto.compilation.NewCompilationDto;
import ru.practicum.dal.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dal.repository.compilation.CompilationRepository;
import ru.practicum.dal.repository.event.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.compilation.CompilationMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;

    private final CompilationMapper compilationMapper;

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(newCompilationDto.getEvents());
        }

        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(events);

        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id " + compId + " не найдена"));

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(request.getEvents());
            compilation.setEvents(events);
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(request.getEvents());
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        compilationRepository.deleteById(compId);
        log.info("Удалена подборка событий с id " + compId);
    }

    @Override
    public List<CompilationDto> get(Boolean pinned, int from, int size) {
        List<Compilation> compilations;
        PageRequest pageRequest = PageRequest.of(from, size);

        if (pinned == null) {
            compilations = compilationRepository.findAllBy(pageRequest);
        } else {
            compilations = compilationRepository.findByPinned(pinned, pageRequest);
        }
        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id " + compId + " не найдена"));
        return compilationMapper.toCompilationDto(compilation);
    }
}
