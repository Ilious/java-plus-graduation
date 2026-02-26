package ru.practicum.ewm.service;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.mapper.HitMapper;
import ru.practicum.ewm.repository.HitRepository;
import ru.practicum.ewm.service.DataBaseConnection.TestContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@ExtendWith(TestContainer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = TestContainer.class)
class StatsServiceImplTest {

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        public StatsServiceImpl statsService(HitRepository repository, HitMapper mapper) {
            return new StatsServiceImpl(repository, mapper);
        }

        @Bean
        @Primary
        public HitMapper mapper() {
            return Mappers.getMapper(HitMapper.class);
        }
    }

    @Autowired
    private StatsServiceImpl statsService;

    private final Faker faker = new Faker();

    private EndpointHit dto;

    private LocalDateTime now;

    private static final String URI = "/address";

    @BeforeEach
    void init() {
        now = LocalDateTime.now();

        dto = EndpointHit.builder()
                .app(faker.book().title())
                .uri(URI)
                .ip(faker.internet().ipV4Address())
                .timestamp(now.minusDays(1))
                .build();
    }

    @Test
    void saveHitTest() {
        EndpointHit saved = assertDoesNotThrow(() -> statsService.saveHit(dto));

        assertNotNull(saved);
        assertAll(() -> {
            assertNotNull(saved.getId());
            assertEquals(dto.getApp(), saved.getApp());
            assertEquals(dto.getIp(), saved.getIp());
            assertEquals(dto.getUri(), saved.getUri());
            assertEquals(dto.getTimestamp(), saved.getTimestamp());
        });
    }

    @Test
    void getStats_UriIsEmptyAndUnique_Test() {
        statsService.saveHit(dto);
        EndpointHit notUniqueHit = EndpointHit.builder()
                .app(dto.getApp())
                .uri(URI)
                .ip(dto.getIp())
                .timestamp(now)
                .build();
        statsService.saveHit(notUniqueHit);

        List<ViewStats> stats = statsService.getStats(now.minusDays(1), now, new ArrayList<>(), true);
        assertNotNull(stats);
        assertAll(() -> {
            assertFalse(stats.isEmpty());
            assertEquals(1, stats.size());
            assertEquals(1, stats.getFirst().getHits());
        });
    }

    @Test
    void getStats_UriIsEmptyAndNotUnique_Test() {
        statsService.saveHit(dto);
        EndpointHit notUniqueHit = EndpointHit.builder()
                .app(dto.getApp())
                .uri(URI)
                .ip(dto.getIp())
                .timestamp(now)
                .build();
        statsService.saveHit(notUniqueHit);

        List<ViewStats> stats = statsService.getStats(now.minusDays(1), now, new ArrayList<>(), false);
        assertNotNull(stats);
        assertAll(() -> {
            assertFalse(stats.isEmpty());
            assertEquals(1, stats.size());
            assertEquals(2, stats.getFirst().getHits());
        });
    }

    @Test
    void getStats_UriIsNotEmptyAndUnique_Test() {
        dto.setUri(URI);
        statsService.saveHit(dto);
        EndpointHit uniqueHit = EndpointHit.builder()
                .app(dto.getApp())
                .uri(URI)
                .ip(dto.getIp())
                .timestamp(now)
                .build();
        statsService.saveHit(uniqueHit);
        EndpointHit notUniqueHit = EndpointHit.builder()
                .app(dto.getApp())
                .uri("/user")
                .ip(dto.getIp())
                .timestamp(now)
                .build();
        statsService.saveHit(notUniqueHit);

        List<ViewStats> stats = statsService.getStats(now.minusDays(1), now, List.of(URI, "/user"), true);
        assertNotNull(stats);
        assertAll(() -> {
            assertFalse(stats.isEmpty());
            assertEquals(2, stats.size());
            assertEquals(
                    1,
                    stats.stream()
                            .filter(it -> it.getUri().equals("/user"))
                            .findFirst()
                            .map(ViewStats::getHits).orElse(-1L)
            );
            assertEquals(
                    1,
                    stats.stream()
                            .filter(it -> it.getUri().equals(URI))
                            .findFirst()
                            .map(ViewStats::getHits).orElse(-1L)
            );
        });
    }

    @Test
    void getStats_UriIsNotEmptyAndNotUnique_Test() {
        dto.setUri(URI);
        statsService.saveHit(dto);
        EndpointHit uniqueHit = EndpointHit.builder()
                .app(dto.getApp())
                .uri(URI)
                .ip(dto.getIp())
                .timestamp(now)
                .build();
        statsService.saveHit(uniqueHit);
        EndpointHit notUniqueHit = EndpointHit.builder()
                .app(dto.getApp())
                .uri("/user")
                .ip(dto.getIp())
                .timestamp(now)
                .build();
        statsService.saveHit(notUniqueHit);

        List<ViewStats> stats = statsService.getStats(now.minusDays(1), now, List.of(URI, "/user"), false);
        assertNotNull(stats);
        assertAll(() -> {
            assertFalse(stats.isEmpty());
            assertEquals(2, stats.size());
            assertEquals(
                    1,
                    stats.stream()
                            .filter(it -> it.getUri().equals("/user"))
                            .findFirst()
                            .map(ViewStats::getHits).orElse(-1L)
            );
            assertEquals(
                    2L,
                    stats.stream()
                            .filter(it -> it.getUri().equals(URI))
                            .findFirst()
                            .map(ViewStats::getHits).orElse(-1L)
            );
        });
    }
}