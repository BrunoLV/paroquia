FROM eclipse-temurin:25-jdk-alpine

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
ENV SONAR_TOKEN=

# Ensure shared directory exists
RUN mkdir -p /shared

# The entrypoint checks if a pre-compiled jar exists in app/build/libs/app.jar (mounted from host workspace)
# to avoid network downloads inside the container; otherwise runs gradle build.
ENTRYPOINT ["/bin/sh", "-c", "if [ -f app/build/libs/app.jar ]; then echo 'Utilizando app.jar pre-compilado...' && cp app/build/libs/app.jar /shared/app.jar; else ./gradlew :app:bootJar -x test --build-cache && cp app/build/libs/app.jar /shared/app.jar; fi"]
