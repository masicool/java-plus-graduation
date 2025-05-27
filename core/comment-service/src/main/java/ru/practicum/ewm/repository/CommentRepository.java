package ru.practicum.ewm.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventId(long eventId, PageRequest page);

    void deleteAllByEventId(long eventId);

    long countByEventId(long eventId);

    List<Comment> findByEventIdIn(List<Long> eventIds);
}
