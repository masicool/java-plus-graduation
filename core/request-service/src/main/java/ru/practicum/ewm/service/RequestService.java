package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.EventRequestShortDto;
import ru.practicum.ewm.dto.request.Status;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> findUserRequests(long userId);

    List<ParticipationRequestDto> findAllRequestsOfEvent(long userId, long eventId);

    ParticipationRequestDto createRequest(long userId, long eventId);

    EventRequestStatusUpdateResult updateStatusForRequestsOfEvent(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    ParticipationRequestDto cancelUserRequest(long userId, long requestId);

    List<EventRequestShortDto> findByEventIdInAndStatus(List<Long> eventIds, Status status);

    long countByEventIdAndStatus(long eventId, Status status);
}
