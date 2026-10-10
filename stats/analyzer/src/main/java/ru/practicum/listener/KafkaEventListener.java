package ru.practicum.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaTopicConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.listener.base.BaseProcessor;
import ru.practicum.service.SimilarityService;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener extends BaseProcessor<String, EventSimilarityAvro>
        implements Runnable {

    private final KafkaTopicConfig kafkaTopicConfig;

    private final Consumer<String, EventSimilarityAvro> consumer;

    private final SimilarityService similarityService;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        consumer.subscribe(Collections.singleton(kafkaTopicConfig.getEventsSimilarity()));

        try {
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(
                        Duration.ofMillis(pollDurationMillis)
                );

                int count = 0;
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    handleRecord(record);

                    manageOffset(record, count, consumer);

                    count++;
                }
            }
        } catch (WakeupException e) {
            log.warn("KafkaEventListener got stop signal. Stopping KafkaEventListener");
        } catch (Exception e) {
            log.error("Error happened in KafkaEventListener during handling records", e);
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
    protected void handleRecord(ConsumerRecord<String, EventSimilarityAvro> record) {
        log.trace("handling record for key {}: {}", record.key(), record.value());
        EventSimilarityAvro eventSimilarity = record.value();
        similarityService.upsertSimilarity(eventSimilarity);
    }
}
