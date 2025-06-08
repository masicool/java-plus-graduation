package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.FindAllEventsPublicParamEntity;
import ru.practicum.ewm.event.model.EventSortAction;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.stats.client.CollectorClient;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.common.Constants.DATE_TIME_PATTERN;
import static ru.practicum.ewm.common.Constants.USER_ID_HEADER;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;
    private final CollectorClient collectorClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findAllEventsPublic(
            @RequestParam(required = false) @Size(min = 1, max = 7000) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSortAction sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            HttpServletRequest request) {
        FindAllEventsPublicParamEntity findAllEventsPublicParamEntity = new FindAllEventsPublicParamEntity(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return eventService.findAllEventsPublic(findAllEventsPublicParamEntity);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findEvent(@RequestHeader(USER_ID_HEADER) long userId, @PathVariable long id) {
        EventFullDto event = eventService.findEvent(id);
        collectorClient.sendPreviewEvent(userId, id);
        return event;
    }

    @PutMapping("/events/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(@RequestHeader(USER_ID_HEADER) long userId, @PathVariable long id) {
        eventService.checkUserRegistrationAtEvent(userId, id);
        collectorClient.sendLikeEvent(userId, id);
    }

    @GetMapping("/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getRecommendationsForUser(@RequestHeader(USER_ID_HEADER) long userId, @RequestParam int maxResults) {
        return eventService.getRecommendationsForUser(userId, maxResults);
    }
}
