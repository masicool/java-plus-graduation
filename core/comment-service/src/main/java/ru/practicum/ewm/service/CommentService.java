package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.dto.comment.CommentFullDto;

import java.util.List;

public interface CommentService {
    CommentFullDto addComment(long userId, long eventId, NewCommentDto newCommentDto);

    CommentFullDto updateComment(long userId, long eventId, long commentId, UpdateCommentDto updateCommentDto);

    CommentFullDto updateCommentAdmin(long commentId, UpdateCommentDto updateCommentDto);

    CommentFullDto findComment(long commentId);

    List<CommentFullDto> findAllEventComments(long eventId, int from, int size);

    void deleteCommentAdmin(long commentId);

    void deleteComment(long userId, long eventId, long commentId);

    void deleteAllEventCommentsAdmin(long eventId);

    List<CommentFullDto> findByEventIdIn(List<Long> eventIds);

    long countByEventId(long eventId);
}
