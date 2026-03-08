package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.dao.Similarity;
import ru.practicum.dal.repository.SimilarityRepo;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.SimilarityMapper;
import ru.practicum.service.SimilarityService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {

    private final SimilarityRepo similarityRepo;

    private final SimilarityMapper similarityMapper;

    @Override
    public void upsertSimilarity(EventSimilarityAvro eventAvro) {
        Long event1 = Math.min(eventAvro.getEventA(), eventAvro.getEventB());
        Long event2 = Math.max(eventAvro.getEventA(), eventAvro.getEventB());

        Similarity similarity = similarityMapper.toEntity(eventAvro, event1, event2);

        similarityRepo.findByEventId1AndEventId2(event1, event2)
                .ifPresentOrElse(entity -> {
                    entity.setSimilarity(similarity.getSimilarity());
                    entity.setTs(similarity.getTs());
                    similarityRepo.save(entity);
                }, () -> similarityRepo.save(similarity));
    }

    @Override
    public List<Similarity> getSimilarEvents(long userId, long eventId, long maxResults) {
        return similarityRepo.findByEventIdOrderBySimilarityDesc(eventId, maxResults);
    }

    @Override
    public List<Similarity> getAllByIdIn(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty())
            return Collections.emptyList();

        return similarityRepo.findByEventIdIn(eventIds);
    }
}
