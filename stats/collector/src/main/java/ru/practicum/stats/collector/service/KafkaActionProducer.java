package ru.practicum.stats.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.stats.collector.config.KafkaTopicConfig;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaActionProducer implements AutoCloseable {

    private final KafkaTopicConfig topicConfig;

    private final Producer<Long, SpecificRecordBase> producer;

    public void send(Long key, Instant ts, SpecificRecordBase data) {
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                topicConfig.getUserActions(),
                null,
                ts.toEpochMilli(),
                key,
                data
        );

        producer.send(record, ((metadata, e) -> {
           if (e != null)
               log.warn("Failed to send message to topic {}", metadata.topic(), e);
           else
               log.info("Successfully sent message to topic {} offset {}", metadata.topic(), metadata.offset());
        }));
    }

    @Override
    public void close(){
       producer.flush();
       producer.close(Duration.ofSeconds(5));
    }
}
