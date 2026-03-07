package ru.practicum.dal.dao;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@Table
@Entity(name = "interactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Double rating;

    private Instant ts;
}
