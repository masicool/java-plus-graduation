package ru.practicum.ewm.api;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.request.EventRequestShortDto;
import ru.practicum.ewm.dto.request.Status;

import java.util.List;
import java.util.Optional;

public interface RequestApi {
    @GetMapping("/find-by-events")
    List<EventRequestShortDto> findByEventIdInAndStatus(@RequestParam List<Long> eventIds, @RequestParam Status status) throws FeignException;

    @GetMapping("/count")
    long countByEventIdAndStatus(@RequestParam long eventId, @RequestParam Status status) throws FeignException;

    @GetMapping("/find-by-requester-and-event")
    Optional<EventRequestShortDto> findByRequesterIdAndEventId(@RequestParam long userId, @RequestParam long eventId) throws FeignException;

}
