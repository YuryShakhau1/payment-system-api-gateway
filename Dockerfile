FROM gradle:8.8-jdk21-alpine AS builder

WORKDIR /src

COPY build.gradle settings.gradle ./

COPY src ./src

RUN gradle clean bootJar --no-daemon


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN chown -R guest:users /app

USER guest

COPY --from=builder /src/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
