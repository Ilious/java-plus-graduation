package ru.practicum.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dal.dao.Similarity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SimilarityRepo extends JpaRepository<Similarity, Long> {

    Optional<Similarity> findByEventId1AndEventId2(Long eventId1, Long eventId2);

    List<Similarity> findAllByEventId1OrEventId2(Long eventId1, Long eventId2);

    List<Similarity> findAllByEventId1InOrEventId2In(Collection<Long> eventId1s, Collection<Long> eventId2s);
}
