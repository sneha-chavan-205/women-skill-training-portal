FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/women-skill-training-portal-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001:10001

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
