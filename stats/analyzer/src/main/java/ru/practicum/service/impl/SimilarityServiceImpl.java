package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.dao.Similarity;
import ru.practicum.dal.repository.SimilarityRepo;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.SimilarityMapper;
import ru.practicum.service.SimilarityService;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {

    private final SimilarityRepo similarityRepo;

    private final SimilarityMapper similarityMapper;

    @Override
    public Similarity upsertSimilarity(EventSimilarityAvro eventAvro) {
        Similarity similarity = similarityMapper.toEntity(eventAvro);

        return similarityRepo.findByEventId1AndEventId2(similarity.getEventId1(), similarity.getEventId2())
                .map(entity -> {
                    entity.setSimilarity(similarity.getSimilarity());
                    entity.setTs(similarity.getTs());
                    return similarityRepo.save(entity);
                }).orElseGet(() -> similarityRepo.save(similarity));
    }

    @Override
    public List<Similarity> getSimilarEvents(long userId, long eventId, long maxResults) {
        return similarityRepo.findAllByEventId1OrEventId2(eventId, eventId);
    }

    @Override
    public List<Similarity> getAllByIdIn(Collection<Long> interactionIds) {
        return similarityRepo.findAllByEventId1InOrEventId2In(interactionIds, interactionIds);
    }
}
