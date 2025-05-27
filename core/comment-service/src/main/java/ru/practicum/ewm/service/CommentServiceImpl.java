package ru.practicum.ewm.service;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.dto.comment.CommentFullDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.State;
import ru.practicum.ewm.dto.request.Status;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.type.BadRequestException;
import ru.practicum.ewm.exception.type.NotFoundException;
import ru.practicum.ewm.exception.type.RemoteServiceException;
import ru.practicum.ewm.feign.EventClient;
import ru.practicum.ewm.feign.RequestClient;
import ru.practicum.ewm.feign.UserClient;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.repository.CommentRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {
    CommentRepository commentRepository;
    UserClient userClient;
    EventClient eventClient;
    RequestClient requestClient;

    @Override
    public CommentFullDto addComment(long userId, long eventId, NewCommentDto newCommentDto) {
        UserShortDto user = receiveUser(userId);
        EventFullDto event = receiveEvent(eventId);
        checkValidEventStatusAndRequester(event, user);
        return CommentMapper.mapToCommentFullDto(commentRepository.save(CommentMapper.mapToComment(event, user, newCommentDto)), user);
    }

    @Override
    public CommentFullDto updateComment(long userId, long eventId, long commentId, UpdateCommentDto updateCommentDto) {
        UserShortDto user = receiveUser(userId);
        EventFullDto event = receiveEvent(eventId);
        Comment comment = receiveComment(commentId);
        checkValidRequester(event, user, comment);
        comment.setText(updateCommentDto.getText());
        return CommentMapper.mapToCommentFullDto(commentRepository.save(comment), user);
    }

    @Override
    public CommentFullDto updateCommentAdmin(long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = receiveComment(commentId);
        comment.setText(updateCommentDto.getText());
        UserShortDto user = receiveUser(comment.getAuthorId());
        return CommentMapper.mapToCommentFullDto(commentRepository.save(comment), user);
    }

    @Transactional(readOnly = true)
    @Override
    public CommentFullDto findComment(long commentId) {
        Comment comment = receiveComment(commentId);
        UserShortDto user = receiveUser(comment.getAuthorId());
        return CommentMapper.mapToCommentFullDto(comment, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentFullDto> findAllEventComments(long eventId, int from, int size) {
        PageRequest page = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, page);

        // получим набор ID всех авторов комментариев
        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        // получим авторов комментариев с помощью сервиса user-service
        Map<Long, UserShortDto> usersMap = receiveUsersByIds(userIds);

        return comments.stream().map(o -> CommentMapper.mapToCommentFullDto(o, usersMap.get(o.getAuthorId()))).toList();
    }

    @Transactional
    @Override
    public void deleteCommentAdmin(long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteComment(long userId, long eventId, long commentId) {
        UserShortDto user = receiveUser(userId);
        EventFullDto event = receiveEvent(eventId);
        Comment comment = receiveComment(commentId);
        checkValidRequester(event, user, comment);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public void deleteAllEventCommentsAdmin(long eventId) {
        commentRepository.deleteAllByEventId(eventId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentFullDto> findByEventIdIn(List<Long> eventIds) {
        List<Comment> comments = commentRepository.findByEventIdIn(eventIds);
        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> usersMap = receiveUsersByIds(userIds);
        return CommentMapper.mapToCommentFullDto(comments, usersMap);
    }

    @Transactional(readOnly = true)
    @Override
    public long countByEventId(long eventId) {
        return commentRepository.countByEventId(eventId);
    }

    private void checkValidEventStatusAndRequester(EventFullDto event, UserShortDto user) {
        if (event.getState() != State.PUBLISHED) {
            throw new BadRequestException("Event with id=" + event.getId() + " must be published");
        }
        if (event.isRequestModeration() || event.getParticipantLimit() != 0) {
            if (event.getInitiator().getId() != user.getId() && requestClient.findByRequesterIdAndEventId(user.getId(), event.getId())
                    .filter(o -> o.getStatus() == Status.CONFIRMED).isEmpty()) {
                throw new BadRequestException("User with id=" + user.getId() + " cannot work with comments");
            }
        }
    }

    private void checkValidRequester(EventFullDto event, UserShortDto user, Comment comment) {
        if (comment.getAuthorId() != user.getId() && event.getInitiator().getId() != user.getId()) {
            throw new BadRequestException("User " + user.getId() + " cannot delete a comment " + comment.getId() + " that is not his own.");
        }
    }

    private Comment receiveComment(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
    }

    private UserShortDto receiveUser(long userId) {
        try {
            return userClient.findById(userId);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NotFoundException("User with ID " + userId + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'warehouse");
        }
    }

    private EventFullDto receiveEvent(long eventId) {
        try {
            return eventClient.findById(eventId);
        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                throw new NotFoundException("Event with ID " + eventId + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'event-service");
        }
    }

    private Map<Long, UserShortDto> receiveUsersByIds(Set<Long> userIds) {
        try {
            return userClient.findByIdIn(userIds.stream().toList()).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
        } catch (FeignException ex) {
            throw new RemoteServiceException("Error in the remote service 'warehouse");
        }
    }
}
