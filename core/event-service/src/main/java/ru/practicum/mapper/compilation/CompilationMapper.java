package ru.practicum.mapper.compilation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.dal.dao.compilation.Compilation;
import ru.practicum.dal.dao.event.Event;
import ru.practicum.dal.dto.compilation.CompilationDto;
import ru.practicum.dal.dto.compilation.NewCompilationDto;
import ru.practicum.dal.dto.compilation.UpdateCompilationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(source = "events", target = "events")
    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto newCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateCompilationFromRequest(UpdateCompilationRequest updateRequest, @MappingTarget Compilation compilation);

    default List<Long> mapEventsToIds(List<Event> events) {
        return events != null ? events.stream()
                .map(Event::getId)
                .toList() : null;
    }
}
