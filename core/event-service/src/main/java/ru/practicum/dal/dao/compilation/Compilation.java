package ru.practicum.dal.dao.compilation;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dal.dao.event.Event;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> events = new ArrayList<>();

    @Column(name = "pinned", nullable = false)
    private Boolean pinned = false;

    @Column(name = "title", nullable = false, length = 50)
    private String title;
}
