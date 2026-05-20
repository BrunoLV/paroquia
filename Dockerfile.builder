FROM eclipse-temurin:21-jdk-alpine

WORKDIR /workspace

# Install necessary tools (if any, like bash)
RUN apk add --no-cache bash

# Copy gradle wrapper and config
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Copy app module source
COPY app app

# Default environment variables
ENV SONAR_HOST_URL=http://sonar:9000
ENV SONAR_TOKEN=

# Ensure shared directory exists
RUN mkdir -p /shared

# The entrypoint will run the build and sonar analysis, then copy the jar to the shared volume
ENTRYPOINT ["/bin/sh", "-c", "./gradlew :app:build :app:sonar -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_TOKEN} && cp app/build/libs/app.jar /shared/app.jar"]
