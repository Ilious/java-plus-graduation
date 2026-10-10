package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaConfigData kafkaConfigData;

    @Bean
    public Consumer<String, EventSimilarityAvro> getEventConsumer() {
        KafkaConfigData.Consumer producerConfig = kafkaConfigData.getConsumer();
        Properties properties = producerConfig.getProperties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-similarity-group");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "ru.practicum.deserializer.EventSimilarityDeserializer");

        return new KafkaConsumer<>(properties);
    }

    @Bean
    public Consumer<Long, UserActionAvro> getActionConsumer() {

        KafkaConfigData.Consumer producerConfig = kafkaConfigData.getConsumer();
        Properties properties = producerConfig.getProperties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-action-group");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "ru.practicum.deserializer.UserActionDeserializer");

        return new KafkaConsumer<>(producerConfig.getProperties());
    }
}
