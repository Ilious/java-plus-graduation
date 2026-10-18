# Explore With Me — Microservices

A Java backend for an event-sharing platform. Users can browse events, register for them, leave comments, and receive event recommendations.

This project continues a [team-developed Explore With Me application](https://github.com/igrshrn/java-explore-with-me-plus). My later work focused on splitting the original application into microservices and adding an event-driven statistics and recommendation system.

## Technologies

- Java, Spring Boot, Spring Cloud (Config Server, Eureka, Gateway)
- Spring Data JPA, PostgreSQL
- Apache Kafka, Avro
- gRPC, Protocol Buffers
- OpenFeign
- Maven, Docker Compose

## Architecture

The application is organized into three main parts:

**Infrastructure**
- **Config Server** — provides configuration for services.
- **Discovery Server (Eureka)** — service registration and discovery.
- **API Gateway** — routes incoming requests to backend services.

**Core services**
- **User Service** — user management.
- **Event Service** — events, categories, and event collections.
- **Request Service** — participation requests.
- **Comment Service** — event comments.
- **Interaction API** — shared contracts and Feign clients for communication between services.

**Statistics and recommendations**
- **Collector** — receives user actions such as views, registrations, and likes, and publishes them to Kafka.
- **Aggregator** — processes user actions and calculates similarity scores between events.
- **Analyzer** — stores interactions and similarity scores and provides recommendation-related operations through gRPC.

## How recommendations work

Each user action has a weight:

| Action | Weight |
| --- | ---: |
| View | 0.4 |
| Register | 0.8 |
| Like | 1.0 |

For each user and event, the Aggregator keeps the highest recorded weight. It estimates how similar two events are by comparing the interest they share across users:

```text
similarity(A, B) = sum(min(weight(user, A), weight(user, B)))
                   / sqrt(sumWeights(A) * sumWeights(B))
```

Instead of recalculating all totals after each action, it updates the stored sums using the difference between old and new weights. Similarity updates are sent to Kafka for the Analyzer.

The Analyzer supports aggregated interaction scores, searches for similar events, and user-based recommendation queries.

## My contribution

The original event platform was built as a team project. In my later version, I worked on:

- Migrating the main application into separate user, event, request, and comment services.
- Adding Spring Cloud Config, Eureka, and API Gateway infrastructure.
- Introducing shared API contracts and Feign-based communication between services.
- Restructuring the multi-module Maven project and updating service configuration.
- Implementing the Kafka-based Collector, Aggregator, and Analyzer modules, including Avro and Protobuf schemas.
- Integrating event similarity and recommendation logic with the event service.
- Fixing service integration, configuration, and build issues.

## Project history

- [Original team repository](https://github.com/igrshrn/java-explore-with-me-plus)
- [Microservices version](https://github.com/Ilious/java-plus-graduation)

This is an educational project. The repository demonstrates backend architecture and integration work; it is not presented as a production-ready recommendation system.
