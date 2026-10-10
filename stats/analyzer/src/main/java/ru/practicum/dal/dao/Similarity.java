package ru.practicum.dal.dao;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Table
@Entity(name = "similarities")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Similarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id1", nullable = false)
    private Long eventId1;

    @Column(name = "event_id2", nullable = false)
    private Long eventId2;

    @Column(nullable = false)
    private Double similarity;

    private Instant ts;
}
