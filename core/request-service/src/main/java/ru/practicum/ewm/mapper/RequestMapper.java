package ru.practicum.ewm.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.request.EventRequestShortDto;
import ru.practicum.ewm.dto.request.Status;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.model.Request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {
    public static Request mapToRequest(UserShortDto requester, EventFullDto event) {
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEventId(event.getId());
        request.setRequesterId(requester.getId());
        request.setStatus(Status.PENDING);
        return request;
    }

    public static ParticipationRequestDto mapToParticipationRequestDto(Request request) {
        return new ParticipationRequestDto(request.getCreated(), request.getEventId(), request.getId(), request.getRequesterId(), request.getStatus());
    }

    public static EventRequestStatusUpdateResult mapToEventRequestStatusUpdateResult(List<Request> confirmedRequests, List<Request> rejectedRequests) {
        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream()
                        .map(RequestMapper::mapToParticipationRequestDto)
                        .toList(),
                rejectedRequests.stream()
                        .map(RequestMapper::mapToParticipationRequestDto)
                        .toList()
        );
    }

    public static EventRequestShortDto mapToEventRequestShortDto(Request request) {
        EventRequestShortDto eventRequestShortDto = new EventRequestShortDto();
        eventRequestShortDto.setId(request.getId());
        eventRequestShortDto.setEventId(request.getEventId());
        eventRequestShortDto.setRequesterId(request.getRequesterId());
        eventRequestShortDto.setStatus(request.getStatus());
        return eventRequestShortDto;
    }

    public static List<EventRequestShortDto> mapToEventRequestShortDto(List<Request> requests) {
        List<EventRequestShortDto> eventRequestShortDtos = new ArrayList<>();
        for (Request request : requests) {
            eventRequestShortDtos.add(mapToEventRequestShortDto(request));
        }
        return eventRequestShortDtos;
    }
}
