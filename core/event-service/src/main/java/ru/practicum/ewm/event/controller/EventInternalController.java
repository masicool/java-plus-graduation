package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.EventApi;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.event.service.EventService;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventInternalController implements EventApi {
    private final EventService eventService;

    @Override
    public EventFullDto findById(long eventId) {
        return eventService.findEventById(eventId);
    }
}
