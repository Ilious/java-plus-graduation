package ru.practicum.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.component.AggregationStarter;

@Component
@RequiredArgsConstructor
public class AggregationRunner implements CommandLineRunner {

    private final AggregationStarter aggregationStarter;

    @Override
    public void run(String... args) {
        aggregationStarter.start();
    }
}
