package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.GetCompilationsParams;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.PatchCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    CompilationDto getCompilation(Long compId);

    List<CompilationDto> getCompilations(GetCompilationsParams params);

    void deleteCompilationById(long compId);

    CompilationDto updateCompilation(PatchCompilationDto patchCompilationDto);
}
