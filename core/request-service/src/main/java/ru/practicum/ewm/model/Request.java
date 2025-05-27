package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.dto.request.Status;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "requests")
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    LocalDateTime created;

    @Column(name = "event_id", nullable = false)
    long eventId;

    @Column(name = "requester_id", nullable = false)
    long requesterId;

    @Enumerated(EnumType.STRING)
    Status status;
}
