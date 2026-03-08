package ru.practicum.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.listener.KafkaActionListener;
import ru.practicum.listener.KafkaEventListener;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

    private final KafkaActionListener actionListener;

    private final KafkaEventListener eventListener;

    @Override
    public void run(String... args) throws Exception {
        Thread actionListenerThread = new Thread(actionListener);
        Thread eventListenerThread = new Thread(eventListener);
        actionListenerThread.setName("KafkaActionListenerThread");
        actionListenerThread.setName("KafkaEventListenerThread");

        actionListenerThread.start();
        eventListenerThread.start();
    }
}
