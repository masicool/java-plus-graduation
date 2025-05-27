package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.comment.CommentFullDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.model.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentMapper {
    public static Comment mapToComment(EventFullDto event, UserShortDto user, NewCommentDto newCommentDto) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setAuthorId(user.getId());
        comment.setEventId(event.getId());
        return comment;
    }

    public static CommentFullDto mapToCommentFullDto(Comment comment, UserShortDto user) {
        CommentFullDto commentFullDto = new CommentFullDto();
        commentFullDto.setId(comment.getId());
        commentFullDto.setText(comment.getText());
        commentFullDto.setAuthor(user);
        commentFullDto.setCreated(comment.getCreated());
        commentFullDto.setLastUpdate(comment.getLastUpdate());
        commentFullDto.setEventId(comment.getEventId());
        return commentFullDto;
    }

    public static List<CommentFullDto> mapToCommentFullDto(List<Comment> comments, Map<Long, UserShortDto> users) {
        List<CommentFullDto> commentFullDtos = new ArrayList<>();
        for (Comment comment : comments) {
            commentFullDtos.add(mapToCommentFullDto(comment, users.get(comment.getAuthorId())));
        }
        return commentFullDtos;
    }
}
