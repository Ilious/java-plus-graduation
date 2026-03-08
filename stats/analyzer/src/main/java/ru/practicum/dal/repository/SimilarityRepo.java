package ru.practicum.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dal.dao.Similarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SimilarityRepo extends JpaRepository<Similarity, Long> {

    Optional<Similarity> findByEventId1AndEventId2(Long eventId1, Long eventId2);

    @Query("SELECT s FROM similarities s WHERE " +
            "s.eventId1 = :eventId OR s.eventId2 = :eventId " +
            "ORDER BY s.similarity DESC LIMIT :limit")
    List<Similarity> findByEventIdOrderBySimilarityDesc(@Param("eventId") Long eventId,
                                                             @Param("limit") long limit);

    @Query("SELECT s FROM similarities s WHERE " +
            "s.eventId1 IN :eventIds OR s.eventId2 IN :eventIds")
    List<Similarity> findByEventIdIn(@Param("eventIds") Set<Long> eventIds);
}