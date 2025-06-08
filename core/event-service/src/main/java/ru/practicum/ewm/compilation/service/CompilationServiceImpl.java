package ru.practicum.ewm.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.GetCompilationsParams;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.PatchCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.stats.client.AnalyzerClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {
    final CompilationRepository compilationRepository;
    final EventRepository eventRepository;
    final ModelMapper modelMapper;
    final AnalyzerClient analyzerClient;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = modelMapper.map(newCompilationDto, Compilation.class);
        if (compilation.getEvents() != null) {
            for (Long eventId : newCompilationDto.getEvents()) {
                compilation.getEvents().add(new Event(eventId));
            }
        }
        // сохранение подборки
        compilation = compilationRepository.save(compilation);
        return getCompilationDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow();
        return getCompilationDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(GetCompilationsParams params) {
        PageRequest pageRequest = PageRequest.of(params.getFrom(), params.getSize());
        List<CompilationDto> compilations = compilationRepository.findAll(pageRequest).stream()
                .map(compilation -> modelMapper.map(compilation, CompilationDto.class))
                .toList();

        // выгрузим информацию по рейтингам мероприятий
        Set<Long> eventIds = compilations.stream()
                .flatMap(e -> e.getEvents().stream())
                .map(EventShortDto::getId).collect(Collectors.toSet());
        Map<Long, Double> ratingsMap = analyzerClient.getInteractionsCount(eventIds.stream().toList());
        compilations.forEach(o -> o.getEvents().forEach(e -> e.setRating(ratingsMap.getOrDefault(e.getId(), 0.0))));

        return compilations;
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(PatchCompilationDto patchCompilationDto) {
        Compilation compilation = compilationRepository.findById(patchCompilationDto.getId()).orElseThrow();
        if (patchCompilationDto.getTitle() != null) {
            compilation.setTitle(patchCompilationDto.getTitle());
        }
        if (patchCompilationDto.getPinned() != null) {
            compilation.setPinned(patchCompilationDto.getPinned());
        }
        if (patchCompilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(patchCompilationDto.getEvents().stream().toList()));
        }
        compilation = compilationRepository.save(compilation);
        return modelMapper.map(compilation, CompilationDto.class);
    }

    @Transactional
    @Override
    public void deleteCompilationById(long compId) {
        compilationRepository.deleteById(compId);
    }

    private CompilationDto getCompilationDto(Compilation compilation) {
        CompilationDto result = modelMapper.map(compilation, CompilationDto.class);
        List<Event> events = eventRepository.getEventsByCompilationId(compilation.getId());
        result.setEvents(events.stream().map(event -> modelMapper.map(event, EventShortDto.class)).toList());

        // выгрузим информацию по рейтингам мероприятий
        Map<Long, Double> ratingsMap = analyzerClient.getInteractionsCount(events.stream().map(Event::getId).toList());
        result.getEvents().forEach(o -> o.setRating(ratingsMap.getOrDefault(o.getId(), 0.0)));

        return result;
    }
}
