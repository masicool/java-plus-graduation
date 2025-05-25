package ru.practicum.ewm.event.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.dto.event.State;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(nullable = false, length = 2000)
    String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(nullable = false, length = 7000)
    String description;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "location_id")
    Location location;

    boolean paid;

    @Column(name = "participant_limit")
    int participantLimit;

    @Column(name = "request_moderation")
    boolean requestModeration;

    @Column(nullable = false, length = 120)
    String title;

    LocalDateTime created;

    @Column(name = "initiator_id", nullable = false)
    long initiatorId;

    @Enumerated(EnumType.STRING)
    State state;

    LocalDateTime published;

    public Event(long id) {
        this.id = id;
    }
}
