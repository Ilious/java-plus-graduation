package ru.practicum.service.compilation;


import ru.practicum.dal.dto.compilation.CompilationDto;
import ru.practicum.dal.dto.compilation.NewCompilationDto;
import ru.practicum.dal.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto newCompilationDto);

    CompilationDto update(Long compId, UpdateCompilationRequest request);

    void delete(Long compId);

    List<CompilationDto> get(Boolean pinned, int from, int size);

    CompilationDto getById(Long compId);
}
