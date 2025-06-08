package ru.practicum.ewm.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.State;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    public static Event mapToEvent(UserShortDto user, Category category, Location location, NewEventDto request) {
        Event event = new Event();
        event.setAnnotation(request.getAnnotation());
        event.setCategory(category);
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setLocation(location);
        event.setPaid(request.isPaid());
        event.setParticipantLimit(request.getParticipantLimit());
        event.setRequestModeration(request.isRequestModeration());
        event.setTitle(request.getTitle());
        event.setCreated(LocalDateTime.now().withNano(0));
        event.setInitiatorId(user.getId());
        event.setState(State.PENDING);
        event.setPublished(null);
        return event;
    }

    public static EventFullDto mapToEventFullDto(Event event, UserShortDto user) {
        EventFullDto dto = new EventFullDto();
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(0);
        dto.setCreatedOn(event.getCreated());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setId(event.getId());
        dto.setInitiator(user);
        dto.setLocation(LocationMapper.mapToLocationDto(event.getLocation()));
        dto.setPaid(event.isPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublished());
        dto.setRequestModeration(event.isRequestModeration());
        dto.setState(event.getState());
        dto.setTitle(event.getTitle());
        dto.setRating(0);
        dto.setComments(0);
        return dto;
    }

    public static EventShortDto mapToEventShortDto(Event event, UserShortDto user) {
        EventShortDto dto = new EventShortDto();
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(0);
        dto.setEventDate(event.getEventDate());
        dto.setId(event.getId());
        dto.setInitiator(user);
        dto.setPaid(event.isPaid());
        dto.setTitle(event.getTitle());
        dto.setRating(0);
        dto.setComments(0);
        return dto;
    }
}
