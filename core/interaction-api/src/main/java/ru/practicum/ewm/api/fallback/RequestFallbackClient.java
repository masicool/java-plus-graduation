package ru.practicum.ewm.api.fallback;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.api.RequestApi;
import ru.practicum.ewm.dto.request.EventRequestShortDto;
import ru.practicum.ewm.dto.request.Status;

import java.util.List;
import java.util.Optional;

@Component
public class RequestFallbackClient implements RequestApi {
    @Override
    public List<EventRequestShortDto> findByEventIdInAndStatus(List<Long> eventIds, Status status) throws FeignException {
        return List.of();
    }

    @Override
    public long countByEventIdAndStatus(long eventId, Status status) throws FeignException {
        return 0;
    }

    @Override
    public Optional<EventRequestShortDto> findByRequesterIdAndEventId(long userId, long eventId) throws FeignException {
        return Optional.empty();
    }
}
