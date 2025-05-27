package ru.practicum.ewm.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.CommentApi;
import ru.practicum.ewm.dto.comment.CommentFullDto;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/internal/comments")
@RequiredArgsConstructor
public class CommentInternalController implements CommentApi {
    private final CommentService commentService;

    @Override
    public CommentFullDto findById(long commentId) throws FeignException {
        return commentService.findComment(commentId);
    }

    @Override
    public List<CommentFullDto> findByEventIdIn(List<Long> eventIds) throws FeignException {
        return commentService.findByEventIdIn(eventIds);
    }

    @Override
    public long countByEventId(long eventId) throws FeignException {
        return commentService.countByEventId(eventId);
    }
}
