FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY .mvn/ ./.mvn/
COPY mvnw ./mvnw
RUN chmod +x ./mvnw

COPY src/ ./src/
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring --home-dir /app spring

COPY --from=build --chown=spring:spring /workspace/target/kodbtw-backend-0.0.1-SNAPSHOT.jar /app/app.jar

ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8080
USER spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
