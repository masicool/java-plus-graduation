package ru.practicum.ewm.api;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.comment.CommentFullDto;

import java.util.List;

public interface CommentApi {
    @GetMapping("/{commentId}")
    CommentFullDto findById(@PathVariable long commentId) throws FeignException;

    @GetMapping
    List<CommentFullDto> findByEventIdIn(@RequestParam List<Long> eventIds) throws FeignException;

    @GetMapping("/find-by-event/{eventId}")
    long countByEventId(@PathVariable long eventId) throws FeignException;
}
