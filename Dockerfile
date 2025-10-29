FROM ubuntu:latest
LABEL authors="emmanuelugwueze"

FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
COPY .mvn .mvn
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-alpine
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
