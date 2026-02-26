package ru.practicum.ewm.service.DataBaseConnection;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Objects;

public class TestContainer implements ApplicationContextInitializer<ConfigurableApplicationContext>,
        AfterAllCallback {

    public static final PostgreSQLContainer container = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        container.start();

        TestPropertyValues.of(
                "spring.datasource.url=" + container.getJdbcUrl(),
                "spring.datasource.username=" + container.getUsername(),
                "spring.datasource.password=" + container.getPassword()
        ).applyTo(applicationContext);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        if (Objects.isNull(container))
            return;

        container.close();
    }
}
