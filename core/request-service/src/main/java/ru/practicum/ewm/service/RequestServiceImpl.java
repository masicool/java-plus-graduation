package ru.practicum.ewm.service;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.UpdateRequestStatus;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.State;
import ru.practicum.ewm.dto.request.EventRequestShortDto;
import ru.practicum.ewm.dto.request.Status;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.type.BadRequestException;
import ru.practicum.ewm.exception.type.ForbiddenException;
import ru.practicum.ewm.exception.type.NotFoundException;
import ru.practicum.ewm.exception.type.RemoteServiceException;
import ru.practicum.ewm.feign.EventClient;
import ru.practicum.ewm.feign.UserClient;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {

    RequestRepository requestRepository;
    UserClient userClient;
    EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> findUserRequests(long userId) {
        receiveUser(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> findAllRequestsOfEvent(long userId, long eventId) {
        UserShortDto user = receiveUser(userId);
        EventFullDto event = receiveEvent(eventId);
        checkInitiatorOfEvent(user, event);
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        UserShortDto user = receiveUser(userId);
        EventFullDto event = receiveEvent(eventId);
        if (event.getInitiator().getId() == userId) {
            throw new ForbiddenException("Request for the event " + eventId + " is not for the initiator " + userId);
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ForbiddenException("User " + userId + " already has request for event " + event);
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ForbiddenException("Event " + eventId + " is not in PUBLISHED");
        }
        if (event.getParticipantLimit() != 0) {
            long amountOfConfirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
            if (event.getParticipantLimit() <= amountOfConfirmedRequests) {
                throw new ForbiddenException("The limit of participants has been reached");
            }
        }
        Request request = RequestMapper.mapToRequest(user, event);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        }
        return RequestMapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusForRequestsOfEvent(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        UserShortDto user = receiveUser(userId);
        EventFullDto event = receiveEvent(eventId);
        checkInitiatorOfEvent(user, event);
        if ((!event.isRequestModeration() || event.getParticipantLimit() == 0) && eventRequestStatusUpdateRequest.getStatus() == UpdateRequestStatus.CONFIRMED) {
            throw new BadRequestException("Confirmation of requests is not required for event " + eventId);
        }
        List<Request> requests = eventRequestStatusUpdateRequest.getRequestIds().stream()
                .map(this::receiveRequest)
                .peek(request -> {
                    if (request.getEventId() != event.getId()) {
                        throw new BadRequestException("Request " + request.getId() + " has not created for event " + eventId);
                    }
                })
                .peek(request -> {
                    if (request.getStatus() != Status.PENDING) {
                        throw new ForbiddenException("Request " + request.getId() + " is not in PENDING");
                    }
                })
                .toList();
        long amountOfConfirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.getParticipantLimit() <= amountOfConfirmedRequests) {
            throw new ForbiddenException("The limit of participants has been reached");
        }
        if (eventRequestStatusUpdateRequest.getStatus() == UpdateRequestStatus.CONFIRMED) {
            List<Request> confirmedRequests = new ArrayList<>();
            List<Request> rejectedRequests = new ArrayList<>();
            for (Request request : requests) {
                if (event.getParticipantLimit() > amountOfConfirmedRequests) {
                    request.setStatus(Status.CONFIRMED);
                    confirmedRequests.add(request);
                    amountOfConfirmedRequests++;
                } else {
                    request.setStatus(Status.REJECTED);
                    rejectedRequests.add(request);
                }
            }
            requestRepository.saveAll(requests);
            return RequestMapper.mapToEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
        } else {
            for (Request request : requests) {
                request.setStatus(Status.REJECTED);
            }
            requestRepository.saveAll(requests);
            return RequestMapper.mapToEventRequestStatusUpdateResult(List.of(), requests);
        }
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(long userId, long requestId) {
        UserShortDto user = receiveUser(userId);
        Request request = receiveRequest(requestId);
        if (request.getRequesterId() != user.getId()) {
            throw new BadRequestException("Request " + request.getId() + " has not made by user " + user.getId());
        }
        request.setStatus(Status.CANCELED);
        return RequestMapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRequestShortDto> findByEventIdInAndStatus(List<Long> eventIds, Status status) {
        return RequestMapper.mapToEventRequestShortDto(requestRepository.findByEventIdInAndStatus(eventIds, status));
    }

    @Override
    @Transactional
    public long countByEventIdAndStatus(long eventId, Status status) {
        return requestRepository.countByEventIdAndStatus(eventId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventRequestShortDto> findByRequesterIdAndEventId(long userId, long eventId) {
        Request request = requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Request with user ID=" + userId + " and event ID=" + eventId + " was not found"));
        return Optional.of(RequestMapper.mapToEventRequestShortDto(request));
    }

    private UserShortDto receiveUser(long userId) {
        try {
            return userClient.findById(userId);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NotFoundException("User with ID " + userId + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'user-service");
        }
    }

    private EventFullDto receiveEvent(long eventId) {
        try {
            return eventClient.findById(eventId);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NotFoundException("Event with ID " + eventId + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'event-service");
        }
    }

    private Request receiveRequest(long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
    }

    private void checkInitiatorOfEvent(UserShortDto user, EventFullDto event) {
        if (event.getInitiator().getId() != user.getId())
            throw new BadRequestException("User " + user.getId() + " is not initiator of event " + event.getId());
    }
}
