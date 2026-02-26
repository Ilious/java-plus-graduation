package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.StatRequest;
import ru.practicum.ewm.dto.ViewStatDto;
import ru.practicum.ewm.exception.ApiError;
import ru.practicum.utils.ResponseGenerator;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StatClient extends ResponseGenerator {

    private final RestClient restClient;

    private final DiscoveryClient discoveryClient;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${stats-server.name:stats-server}")
    private String statServiceId;

    public StatClient(DiscoveryClient discoveryClient) {
        restClient = RestClient.create();
        this.discoveryClient = discoveryClient;
    }

    private ServiceInstance getInstance() {
        List<ServiceInstance> instances = discoveryClient.getInstances(statServiceId);

        if (instances == null || instances.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Ошибка поиска сервиса статстики с id %s", statServiceId)
            );
        }

        ServiceInstance instance = instances.getFirst();
        log.debug("Обнаружен сервис {}:{}", instance.getHost(), instance.getPort());
        return instance;
    }

    private URI getStatsServiceUri(String path) {
        ServiceInstance instance = getInstance();
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(instance.getHost())
                .port(instance.getPort())
                .path(path)
                .build()
                .toUri();
    }

    public ResponseEntity<Object> saveHit(EndpointHit hit) {
        try {
            log.info("Сохранение информации о запросе {}", hit);
            URI hitUri = getStatsServiceUri("/hit");

            return makeResult(restClient.post()
                    .uri(hitUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hit)
                    .retrieve()
                    .body(EndpointHit.class), HttpStatus.CREATED);
        } catch (Exception e) {
            String msg = "Oшибка при сохранении информации";
            log.error(msg + " {}", e.getMessage(), e);
            return makeResult(ApiError.builder()
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<ViewStatDto> getStats(StatRequest request) {
        if (request == null || !request.isValid()) {
            log.warn("Некорректные параметры запроса статистики");
            return Collections.emptyList();
        }

        try {
            log.info("Запрос статистики {}", request);

            URI statsUri = getStatsServiceUri("/stats");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(statsUri)
                    .queryParam("start", request.getStart().format(FORMATTER))
                    .queryParam("end", request.getEnd().format(FORMATTER))
                    .queryParam("unique", request.getUnique());

            if (request.getUris() != null && !request.getUris().isEmpty()) {
                String uris = String.join(",", request.getUris());
                builder.queryParam("uris", uris);
            }

            ResponseEntity<List<ViewStatDto>> response = restClient.get()
                    .uri(builder.build().toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("Ошибка при получении статистики: {}", response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("Ошибка при запросе статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}