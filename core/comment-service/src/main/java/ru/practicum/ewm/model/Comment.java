package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String text;

    @Column(name = "event_id", nullable = false)
    long eventId;

    @Column(name = "author_id", nullable = false)
    long authorId;

    @CreationTimestamp
    LocalDateTime created;

    @UpdateTimestamp
    LocalDateTime lastUpdate;
}
