package ru.practicum.ewm.api.fallback;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.api.EventApi;
import ru.practicum.ewm.dto.event.EventFullDto;

@Component
public class EventFallbackClient implements EventApi {
    @Override
    public EventFullDto findById(long eventId) throws FeignException {
        return EventFullDto.builder().id(eventId).build();
    }
}
