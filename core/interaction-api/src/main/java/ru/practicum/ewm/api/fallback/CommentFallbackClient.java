package ru.practicum.ewm.api.fallback;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.api.CommentApi;
import ru.practicum.ewm.dto.comment.CommentFullDto;

import java.util.List;

@Component
public class CommentFallbackClient implements CommentApi {
    @Override
    public CommentFullDto findById(long commentId) throws FeignException {
        return null;
    }

    @Override
    public List<CommentFullDto> findByEventIdIn(List<Long> eventIds) throws FeignException {
        return List.of();
    }

    @Override
    public long countByEventId(long eventId) throws FeignException {
        return 0;
    }
}
