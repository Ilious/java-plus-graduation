package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter @ToString
@ConfigurationProperties("server.kafka.topic-config")
public class KafkaTopicConfig {

    private String userActions;

    private String eventsSimilarity;
}
