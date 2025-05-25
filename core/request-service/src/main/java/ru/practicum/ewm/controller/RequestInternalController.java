package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.RequestApi;
import ru.practicum.ewm.dto.request.EventRequestShortDto;
import ru.practicum.ewm.dto.request.Status;
import ru.practicum.ewm.service.RequestService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class RequestInternalController implements RequestApi {

    private final RequestService requestService;

    @Override
    public List<EventRequestShortDto> findByEventIdInAndStatus(List<Long> eventIds, Status status) {
        return requestService.findByEventIdInAndStatus(eventIds, status);
    }

    @Override
    public long countByEventIdAndStatus(long eventId, Status status) {
        return requestService.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public Optional<EventRequestShortDto> findByRequesterIdAndEventId(long userId, long eventId) {
        return Optional.empty();
    }
}
