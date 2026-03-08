package ru.practicum.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dal.dao.Interaction;
import ru.practicum.dal.dao.RecommendedEventProjection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InteractionRepo extends JpaRepository<Interaction, Long> {

    @Query("""
            SELECT i.eventId AS eventId, sum(i.rating) AS score FROM interactions i
            WHERE i.eventId IN :eventIds
            GROUP BY i.eventId
            """)
    List<RecommendedEventProjection> findSumWeightsByEventId(Collection<Long> eventIds);

    List<Long> findAllByEventIdIn(Collection<Long> eventIds);

    List<Interaction> findAllByUserId(Long userId);

    Optional<Interaction> findByEventIdAndUserId(Long eventId, Long userId);
}
