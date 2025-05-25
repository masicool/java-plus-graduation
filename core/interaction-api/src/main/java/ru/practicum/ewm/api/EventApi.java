package ru.practicum.ewm.api;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.dto.event.EventFullDto;

public interface EventApi {
    @GetMapping("/{eventId}")
    EventFullDto findById(@PathVariable long eventId) throws FeignException;
}
