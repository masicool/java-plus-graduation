package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.ewm.stats.client.CollectorClient;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;
    private final CollectorClient collectorClient;

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findUserRequests(@PathVariable long userId) {
        return requestService.findUserRequests(userId);
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findAllRequestsOfEvent(@PathVariable long userId,
                                                                @PathVariable long eventId) {
        return requestService.findAllRequestsOfEvent(userId, eventId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable long userId,
                                                 @RequestParam long eventId) {
        ParticipationRequestDto request = requestService.createRequest(userId, eventId);
        collectorClient.sendRegistrationEvent(userId, eventId);
        return request;
    }

    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatusForRequestsOfEvent(@PathVariable long userId,
                                                                         @PathVariable long eventId,
                                                                         @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return requestService.updateStatusForRequestsOfEvent(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelUserRequest(@PathVariable long userId,
                                                     @PathVariable long requestId) {
        return requestService.cancelUserRequest(userId, requestId);
    }
}
