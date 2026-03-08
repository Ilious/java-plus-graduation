package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.dao.Interaction;
import ru.practicum.dal.dao.RecommendedEventProjection;
import ru.practicum.dal.repository.InteractionRepo;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.InteractionMapper;
import ru.practicum.service.InteractionService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepo interactionRepo;

    private final InteractionMapper interactionMapper;

    @Override
    public List<RecommendedEventProjection> getSumWeightsByEventId(Set<Long> ids) {
        return interactionRepo.findSumWeightsByEventId(ids);
    }

    @Override
    public void upsertInteraction(UserActionAvro action) {
        Interaction interaction = interactionMapper.toEntity(action);

        interactionRepo.findByEventIdAndUserId(action.getEventId(), action.getUserId())
                .map(entity -> {
                    if (entity.getRating() < interaction.getRating()) {
                        entity.setRating(interaction.getRating());
                        entity.setTs(interaction.getTs());
                        return interactionRepo.save(entity);
                    }
                    return interaction;
                })
                .orElseGet(() -> interactionRepo.save(interaction));
    }

    @Override
    public List<Interaction> getAllByUserId(long userId) {
        return interactionRepo.findAllByUserId(userId);
    }

    @Override
    public List<Long> getAllIdsById(Set<Long> similarEventIds) {
        return interactionRepo.findAllByEventIdIn(similarEventIds);
    }
}
