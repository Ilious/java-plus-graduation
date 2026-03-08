package ru.practicum.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaTopicConfig;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.listener.base.BaseProcessor;
import ru.practicum.service.InteractionService;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaActionListener extends BaseProcessor<Long, UserActionAvro>
        implements Runnable {

    private final KafkaTopicConfig kafkaTopicConfig;

    private final InteractionService interactionService;

    private final Consumer<Long, UserActionAvro> consumer;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        consumer.subscribe(Collections.singleton(kafkaTopicConfig.getUserActions()));

        try {
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(
                        Duration.ofMillis(pollDurationMillis)
                );

                int count = 0;
                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    handleRecord(record);

                    manageOffset(record, count, consumer);

                    count++;
                }
            }
        } catch (WakeupException e) {
            log.warn("KafkaActionListener got stop signal. Stopping KafkaActionListener");
        } catch (Exception e) {
            log.error("Error happened in KafkaActionListener during handling records", e);
        } finally {
            try {
                consumer.commitAsync();
            } finally {
                log.info("Closing consumer");
                consumer.close();
            }
        }
    }

    @Override
    protected void handleRecord(ConsumerRecord<Long, UserActionAvro> record) {
        log.trace("handling record for key {}: {}", record.key(), record.value());
        UserActionAvro userActionAvro = record.value();
        interactionService.upsertInteraction(userActionAvro);
    }
}

