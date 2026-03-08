package ru.practicum.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.component.base.BaseProcessor;
import ru.practicum.config.KafkaTopicConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.EventSimilarityService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter extends BaseProcessor<UserActionAvro> {

    private final KafkaTopicConfig topicConfig;

    private final Consumer<Long, UserActionAvro> consumer;

    private final Producer<String, EventSimilarityAvro> producer;

    private final EventSimilarityService eventSimilarityService;

    public void start() {
        try {
            consumer.subscribe(List.of(topicConfig.getUserActions()));

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(
                        Duration.ofMillis(pollDurationMillis)
                );

                int count = 0;

                for (ConsumerRecord<Long, UserActionAvro> record: records) {
                    handleRecord(record);

                    manageOffset(record, count, consumer);

                    count++;
                }
            }

        } catch (WakeupException e) {
            log.warn("AggregationStarter got stop signal. Stopping AggregationStarter");
        } catch (Exception e) {
            log.error("Error happened in AggregationStarter during handling records", e);
        } finally {
            try {
                producer.flush();
                consumer.commitAsync();
            } finally {
                log.info("Closing consumer");
                consumer.close();
                log.info("Closing producer");
                producer.close();
            }
        }
    }

    @Override
    protected void handleRecord(ConsumerRecord<Long, UserActionAvro> record) {
        log.trace("Handling Record: topic {}, partition {}, offset {}, value {}",
                record.topic(), record.partition(), record.offset(), record.value());

        List<EventSimilarityAvro> weightSums = eventSimilarityService.calculateWeightSums(record.value());
        for (EventSimilarityAvro eventSimilarity : weightSums) {
            ProducerRecord<String, EventSimilarityAvro> producerRecord = new ProducerRecord<>(
                    topicConfig.getEventsSimilarity(),
                    null,
                    eventSimilarity.getTimestamp().toEpochMilli(),
                    "%s - %s".formatted(eventSimilarity.getEventA(), eventSimilarity.getEventB()),
                    eventSimilarity
            );

            producer.send(producerRecord);
        }
    }
}
