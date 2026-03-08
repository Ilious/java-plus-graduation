package ru.practicum.stats.collector.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaConfigData kafkaConfigData;

    @Bean
    public Producer<Long, SpecificRecordBase> getProducer() {
        KafkaConfigData.Producer producerConfig = kafkaConfigData.getProducer();
        return new KafkaProducer<>(producerConfig.getProperties());
    }
}
