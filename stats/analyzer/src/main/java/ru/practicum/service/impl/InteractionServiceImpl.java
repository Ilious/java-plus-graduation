package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.dao.Interaction;
import ru.practicum.dal.dao.RecommendedEventProjection;
import ru.practicum.dal.repository.InteractionRepo;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.InteractionMapper;
import ru.practicum.service.InteractionService;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepo interactionRepo;

    private final InteractionMapper interactionMapper;

    @Override
    public List<Long> getAllIdsById(Collection<Long> eventIds) {
        return interactionRepo.findAllByEventIdIn(eventIds);
    }

    @Override
    public List<Interaction> getAllByUserId(Long userId) {
        return interactionRepo.findAllByUserId(userId);
    }

    @Override
    public List<RecommendedEventProjection> getSumWeightsByEventId(Collection<Long> ids) {
        return interactionRepo.findSumWeightsByEventId(ids);
    }

    @Override
    public Interaction upsertInteraction(UserActionAvro action) {
        Interaction interaction = interactionMapper.toEntity(action);

        return interactionRepo.findByEventIdAndUserId(action.getEventId(), action.getUserId())
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
}
