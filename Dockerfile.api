FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# The compiled jar is expected to be present in this directory
# (mapped from a shared volume populated by the builder container)

EXPOSE 8080

# Environment variables with defaults (overridden by docker-compose)
ENV DB_URL=jdbc:postgresql://db:5432/calendario
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
