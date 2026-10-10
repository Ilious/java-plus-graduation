package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaConfigData kafkaConfigData;

    @Bean
    public Producer<String, EventSimilarityAvro> getProducer() {
        KafkaConfigData.Producer producer = kafkaConfigData.getProducer();

        return new KafkaProducer<>(producer.getProperties());
    }

    @Bean
    public Consumer<Long, UserActionAvro> getConsumer() {
        KafkaConfigData.Consumer consumer = kafkaConfigData.getConsumer();

        return new KafkaConsumer<>(consumer.getProperties());
    }
}
