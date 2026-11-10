# =============================================================================
# Etapa 1 — compilación (Gradle + JDK 21)
# =============================================================================
FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace

# Copiamos primero los descriptores para aprovechar la caché de capas
COPY settings.gradle build.gradle ./
COPY src ./src

RUN gradle --no-daemon clean bootJar -x test

# =============================================================================
# Etapa 2 — ejecución (JRE 21, usuario no root)
# =============================================================================
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r app && useradd -r -g app app
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080
ENV JAVA_OPTS=""

HEALTHCHECK --interval=15s --timeout=5s --retries=10 --start-period=30s \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
