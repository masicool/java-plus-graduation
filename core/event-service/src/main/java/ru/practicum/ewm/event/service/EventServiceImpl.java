package ru.practicum.ewm.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.dto.comment.CommentFullDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.LocationDto;
import ru.practicum.ewm.dto.event.State;
import ru.practicum.ewm.dto.request.EventRequestShortDto;
import ru.practicum.ewm.dto.request.Status;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.type.BadRequestException;
import ru.practicum.ewm.exception.type.ForbiddenException;
import ru.practicum.ewm.exception.type.NotFoundException;
import ru.practicum.ewm.exception.type.RemoteServiceException;
import ru.practicum.ewm.feign.CommentClient;
import ru.practicum.ewm.feign.RequestClient;
import ru.practicum.ewm.feign.UserClient;
import ru.practicum.ewm.stats.client.AnalyzerClient;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    LocationRepository locationRepository;
    UserClient userClient;
    CategoryRepository categoryRepository;
    RequestClient requestClient;
    CommentClient commentClient;
    AnalyzerClient analyzerClient;

    @Override
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("EventDate must be after now");
        }
        UserShortDto user = receiveUser(userId);
        Category category = receiveCategory(newEventDto.getCategory());
        Location location = addLocation(newEventDto.getLocation()); // TODO Под фичу нужно будет пересмотреть эту логику, а пока что так.
        Event event = EventMapper.mapToEvent(user, category, location, newEventDto);
        return EventMapper.mapToEventFullDto(eventRepository.save(event), user);
    }

    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequestDto updateEventUserRequestDto) {
        UserShortDto user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkValidUserForEvent(user, event);
        if (event.getState() == State.PUBLISHED) {
            throw new ForbiddenException("Event must not be published");
        }
        UpdateEventFieldsEntity updateEventFieldsEntity = new UpdateEventFieldsEntity(updateEventUserRequestDto.getAnnotation(),
                updateEventUserRequestDto.getCategory(),
                updateEventUserRequestDto.getDescription(),
                updateEventUserRequestDto.getEventDate(),
                updateEventUserRequestDto.getLocation(),
                updateEventUserRequestDto.getPaid(),
                updateEventUserRequestDto.getParticipantLimit(),
                updateEventUserRequestDto.getRequestModeration(),
                updateEventUserRequestDto.getTitle()
        );
        updateFields(event, updateEventFieldsEntity);
        if (updateEventUserRequestDto.getStateAction() != null) {
            switch (updateEventUserRequestDto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }
        return EventMapper.mapToEventFullDto(eventRepository.save(event), user);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto findOwnersEventById(long userId, long eventId) {
        UserShortDto user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkValidUserForEvent(user, event);
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(event, user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> findOwnersEvents(long userId, int from, int size) {
        UserShortDto user = receiveUser(userId);
        PageRequest page = PageRequest.of(from, size);
        return loadStatisticAndRequestForList(eventRepository.findAllByInitiatorId(user.getId(), page).stream()
                .map(o -> EventMapper.mapToEventFullDto(o, user))
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> findAllEvents(FindAllEventsParamEntity findAllEventsParamEntity) {
        Predicate predicate = predicateForFindingAllEventsByAdmin(findAllEventsParamEntity);
        PageRequest page = PageRequest.of(findAllEventsParamEntity.getFrom(), findAllEventsParamEntity.getSize());

        // получим список событий по условиям
        Page<Event> events = eventRepository.findAll(predicate, page);
        // получим набор ID всех инициаторов событий
        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        // получим инициаторов событий с помощью сервиса user-service
        Map<Long, UserShortDto> usersMap = receiveUsersByIds(userIds);

        return loadStatisticAndRequestForList(events.stream()
                .map(o -> EventMapper.mapToEventFullDto(o, usersMap.get(o.getInitiatorId())))
                .toList());
    }

    @Override
    public EventFullDto editEvent(long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        Event event = receiveEvent(eventId);
        if (updateEventAdminRequestDto.getEventDate() != null && updateEventAdminRequestDto.getEventDate().isBefore(event.getCreated().plusHours(1))) {
            throw new BadRequestException("Date of event cannot be before created date!");
        }
        UpdateEventFieldsEntity updateEventFieldsEntity = new UpdateEventFieldsEntity(updateEventAdminRequestDto.getAnnotation(),
                updateEventAdminRequestDto.getCategory(),
                updateEventAdminRequestDto.getDescription(),
                updateEventAdminRequestDto.getEventDate(),
                updateEventAdminRequestDto.getLocation(),
                updateEventAdminRequestDto.getPaid(),
                updateEventAdminRequestDto.getParticipantLimit(),
                updateEventAdminRequestDto.getRequestModeration(),
                updateEventAdminRequestDto.getTitle()
        );
        updateFields(event, updateEventFieldsEntity);
        if (updateEventAdminRequestDto.getStateAction() != null) {
            if (event.getState() != State.PENDING) {
                throw new ForbiddenException("Cannot publish the event because it is not in PENDING");
            }
            switch (updateEventAdminRequestDto.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(State.PUBLISHED);
                    event.setPublished(LocalDateTime.now());
                }
                case REJECT_EVENT -> event.setState(State.CANCELED);
            }
        }
        UserShortDto userShortDto = receiveUser(event.getInitiatorId());
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(eventRepository.save(event), userShortDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> findAllEventsPublic(FindAllEventsPublicParamEntity findAllEventsPublicParamEntity) {
        LocalDateTime rangeEnd = findAllEventsPublicParamEntity.getRangeEnd();
        LocalDateTime rangeStart = findAllEventsPublicParamEntity.getRangeStart();
        if (rangeEnd != null && rangeStart != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BadRequestException("'rangeEnd' can not be before 'rangeStart'");
            }
        }
        Predicate predicate = predicateForFindingAllEventsByAny(findAllEventsPublicParamEntity);
        PageRequest page = PageRequest.of(findAllEventsPublicParamEntity.getFrom(), findAllEventsPublicParamEntity.getSize());
        List<Event> events = eventRepository.findAll(predicate, page).stream().toList();
        if (findAllEventsPublicParamEntity.isOnlyAvailable()) {
            events = events.stream()
                    .filter(this::isEventAvailableByLimit)
                    .toList();
        }
        List<EventShortDto> eventShortDtos = loadStatisticAndRequest(events);
        if (findAllEventsPublicParamEntity.getSort() != null) {
            switch (findAllEventsPublicParamEntity.getSort()) {
                case EVENT_DATE -> eventShortDtos = eventShortDtos.stream()
                        .sorted(Comparator.comparing(EventShortDto::getEventDate))
                        .toList();
                case VIEWS -> eventShortDtos = eventShortDtos.stream()
                        .sorted(Comparator.comparing(EventShortDto::getRating))
                        .toList();
            }
        }
        return eventShortDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto findEvent(long eventId) {
        Event event = receiveEvent(eventId);
        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        UserShortDto userShortDto = receiveUser(event.getInitiatorId());
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(event, userShortDto));
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto findEventById(long eventId) {
        Event event = receiveEvent(eventId);
        UserShortDto userShortDto = receiveUser(event.getInitiatorId());
        return EventMapper.mapToEventFullDto(event, userShortDto);
    }

    @Override
    public void checkUserRegistrationAtEvent(long userId, long eventId) {
        Event event = receiveEvent(eventId);
        receiveUser(userId);
        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " not published");
        }
        if (event.isRequestModeration() || event.getParticipantLimit() != 0) {
            if (event.getInitiatorId() != userId && requestClient.findByRequesterIdAndEventId(userId, eventId)
                    .filter(o -> o.getStatus() == Status.CONFIRMED).isEmpty()) {
                throw new BadRequestException("User with id=" + userId + " cannot work with event ID=" + event);
            }
        }
    }

    @Override
    public List<EventFullDto> getRecommendationsForUser(long userId, int maxResult) {
        receiveUser(userId); // проверим, что пользователь такой есть
        // получи список ID рекомендованных мероприятий для пользователя
        List<Long> eventIds = analyzerClient.getRecommendationsForUser(userId, maxResult)
                .map(RecommendedEventProto::getEventId).toList();
        // выгрузим мероприятия по этому списку
        List<Event> events = eventRepository.findByIdIn(eventIds);
        // получим ID инициаторов мероприятий
        Set<Long> initiatorIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        // выгрузим инициаторов в мапу
        Map<Long, UserShortDto> initiatorsMap = receiveUsers(initiatorIds.stream().toList()).stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
        // замапим мероприятия в список DTO
        List<EventFullDto> eventsFullDto = events.stream()
                .map(o -> EventMapper.mapToEventFullDto(o, initiatorsMap.get(o.getInitiatorId()))).toList();
        // загрузим статистику, рейтинги, запросы
        loadStatisticAndRequestForList(eventsFullDto);

        return eventsFullDto;
    }

    private void updateFields(Event event, UpdateEventFieldsEntity updateEventFieldsEntity) {
        if (updateEventFieldsEntity.hasEventDate()) {
            if (updateEventFieldsEntity.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("The date and time of event cannot be earlier than two hours from the current moment.");
            }
            event.setEventDate(updateEventFieldsEntity.getEventDate());
        }
        if (updateEventFieldsEntity.hasAnnotation()) {
            event.setAnnotation(updateEventFieldsEntity.getAnnotation());
        }
        if (updateEventFieldsEntity.hasCategoryId()) {
            Category category = receiveCategory(updateEventFieldsEntity.getCategoryId());
            event.setCategory(category);
        }
        if (updateEventFieldsEntity.hasDescription()) {
            event.setDescription(updateEventFieldsEntity.getDescription());
        }
        if (updateEventFieldsEntity.hasLocation()) { //TODO Эту реализацию также изменить под фичу.
            //  (Тут id локации такой же, меняются значения)
            event.setLocation(locationRepository.save(LocationMapper.updateLocationFields(event.getLocation(), updateEventFieldsEntity.getLocation())));
        }
        if (updateEventFieldsEntity.hasPaid()) {
            event.setPaid(updateEventFieldsEntity.getPaid());
        }
        if (updateEventFieldsEntity.hasParticipantLimit()) {
            event.setParticipantLimit(updateEventFieldsEntity.getParticipantLimit());
        }
        if (updateEventFieldsEntity.hasRequestModeration()) {
            event.setRequestModeration(updateEventFieldsEntity.getRequestModeration());
        }
        if (updateEventFieldsEntity.hasTitle()) {
            event.setTitle(updateEventFieldsEntity.getTitle());
        }
    }

    private List<EventShortDto> loadStatisticAndRequest(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        List<EventRequestShortDto> requests = requestClient.findByEventIdInAndStatus(events.stream()
                .map(Event::getId)
                .toList(), Status.CONFIRMED);

        // получим рейтинг мероприятия из сервиса и обновим его в DTO
        Map<Long, Double> ratingsMap = analyzerClient.getInteractionsCount(events.stream().map(Event::getId).toList());

        List<CommentFullDto> comments = commentClient.findByEventIdIn(events.stream()
                .map(Event::getId)
                .toList());

        // получим набор ID всех инициаторов событий
        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        // получим владельцев событий с помощью сервиса user-service
        Map<Long, UserShortDto> usersMap = receiveUsersByIds(userIds);

        return events.stream()
                .map(o -> EventMapper.mapToEventShortDto(o, usersMap.get(o.getInitiatorId())))
                .peek(event -> event.setConfirmedRequests(requests.stream()
                        .filter(request -> request.getEventId() == event.getId())
                        .count()))
                .peek(event -> event.setRating(ratingsMap.getOrDefault(event.getId(), 0.0)))
                .peek(event -> event.setComments(comments.stream()
                        .filter(comment -> comment.getEventId() == event.getId())
                        .count()))
                .toList();
    }

    private EventFullDto loadStatisticAndRequest(EventFullDto event) {
        long amountOfConfirmedRequests = requestClient.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);
        event.setConfirmedRequests(amountOfConfirmedRequests);

        // получим рейтинг мероприятия из сервиса и обновим его в DTO
        Map<Long, Double> ratingsMap = analyzerClient.getInteractionsCount(List.of(event.getId()));
        event.setRating(ratingsMap.getOrDefault(event.getId(), 0.0));

        long amountOfViews = 0;
        event.setRating(amountOfViews);
        long amountOfComments = commentClient.countByEventId(event.getId());
        event.setComments(amountOfComments);
        return event;
    }

    private List<EventFullDto> loadStatisticAndRequestForList(List<EventFullDto> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        List<EventRequestShortDto> requests = requestClient.findByEventIdInAndStatus(events.stream()
                .map(EventFullDto::getId)
                .toList(), Status.CONFIRMED);

        // получим рейтинги мероприятий из сервиса
        Map<Long, Double> ratingsMap = analyzerClient.getInteractionsCount(events.stream().map(EventFullDto::getId).collect(Collectors.toList()));

        List<CommentFullDto> comments = commentClient.findByEventIdIn(events.stream()
                .map(EventFullDto::getId)
                .toList());
        return events.stream()
                .peek(event -> event.setConfirmedRequests(requests.stream()
                        .filter(request -> request.getEventId() == event.getId())
                        .count()))
                .peek(event -> event.setRating(ratingsMap.getOrDefault(event.getId(), 0.0)))
                .peek(event -> event.setComments(comments.stream()
                        .filter(comment -> comment.getEventId() == event.getId())
                        .count()))
                .toList();
    }

    private Predicate predicateForFindingAllEventsByAdmin(FindAllEventsParamEntity findAllEventsParamEntity) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (findAllEventsParamEntity.getUsers() != null && !findAllEventsParamEntity.getUsers().isEmpty()) {
            booleanBuilder.and(QEvent.event.initiatorId.in(findAllEventsParamEntity.getUsers()));
        }
        if (findAllEventsParamEntity.getStates() != null && !findAllEventsParamEntity.getStates().isEmpty()) {
            booleanBuilder.and(QEvent.event.state.in(findAllEventsParamEntity.getStates()));
        }
        if (findAllEventsParamEntity.getCategories() != null && !findAllEventsParamEntity.getCategories().isEmpty()) {
            booleanBuilder.and(QEvent.event.category.id.in(findAllEventsParamEntity.getCategories()));
        }
        if (findAllEventsParamEntity.getRangeStart() != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(findAllEventsParamEntity.getRangeStart()));
        }
        if (findAllEventsParamEntity.getRangeEnd() != null) {
            booleanBuilder.and(QEvent.event.eventDate.before(findAllEventsParamEntity.getRangeEnd()));
        }
        if (!booleanBuilder.hasValue()) booleanBuilder.and(QEvent.event.id.isNotNull());
        return booleanBuilder.getValue();
    }

    private Predicate predicateForFindingAllEventsByAny(FindAllEventsPublicParamEntity findAllEventsPublicParamEntity) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(QEvent.event.state.eq(State.PUBLISHED));
        if (findAllEventsPublicParamEntity.getText() != null && !findAllEventsPublicParamEntity.getText().isBlank()) {
            booleanBuilder.and(QEvent.event.description.containsIgnoreCase(findAllEventsPublicParamEntity.getText())
                    .or(QEvent.event.annotation.containsIgnoreCase(findAllEventsPublicParamEntity.getText())));
        }
        if (findAllEventsPublicParamEntity.getCategories() != null && !findAllEventsPublicParamEntity.getCategories().isEmpty()) {
            booleanBuilder.and(QEvent.event.category.id.in(findAllEventsPublicParamEntity.getCategories()));
        }
        if (findAllEventsPublicParamEntity.getPaid() != null) {
            booleanBuilder.and(QEvent.event.paid.eq(findAllEventsPublicParamEntity.getPaid()));
        }
        if (findAllEventsPublicParamEntity.getRangeStart() != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(findAllEventsPublicParamEntity.getRangeStart()));
        }
        if (findAllEventsPublicParamEntity.getRangeEnd() != null) {
            booleanBuilder.and(QEvent.event.eventDate.before(findAllEventsPublicParamEntity.getRangeEnd()));
        }
        if (findAllEventsPublicParamEntity.getRangeStart() != null && findAllEventsPublicParamEntity.getRangeEnd() != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }
        return booleanBuilder.getValue();
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

    private List<UserShortDto> receiveUsers(List<Long> userIds) {
        try {
            return userClient.findByIdIn(userIds);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NotFoundException("Same users with IDs " + userIds + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'user-service");
        }
    }

    private Map<Long, UserShortDto> receiveUsersByIds(Set<Long> userIds) {
        try {
            return userClient.findByIdIn(userIds.stream().toList()).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
        } catch (FeignException ex) {
            throw new RemoteServiceException("Error in the remote service 'user-service");
        }
    }

    private Category receiveCategory(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }

    private Event receiveEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void checkValidUserForEvent(UserShortDto user, Event event) {
        if (event.getInitiatorId() != user.getId())
            throw new BadRequestException("Event is not for this user");
    }

    private boolean isEventAvailableByLimit(Event event) {
        return event.getParticipantLimit() > requestClient.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);
    }

    private Location addLocation(LocationDto locationDto) {
        return locationRepository.save(LocationMapper.mapToLocation(locationDto));
    }
}
