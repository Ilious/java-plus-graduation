FROM openjdk:21-ea-21-slim-bullseye
WORKDIR /app
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]