# ---- Stage 1: build -------------------------------------------------------
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /build

# Dependencies first, so the Maven cache layer survives source-only changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

# Build, then normalise the boot jar to a stable name (the repackage step
# also emits a *.jar.original, so we can't just glob into the runtime stage).
RUN mvn -B clean package -DskipTests \
 && JAR=$(find target -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n1) \
 && cp "$JAR" /build/app.jar \
 && echo "boot jar: $JAR"

# ---- Stage 2: runtime -----------------------------------------------------
FROM eclipse-temurin:26-jre-noble AS runtime

# curl is only here for the HEALTHCHECK below.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# Never run the app as root.
RUN groupadd --system --gid 1001 improv \
 && useradd --system --uid 1001 --gid improv --create-home --shell /usr/sbin/nologin improv

WORKDIR /app
COPY --from=build --chown=improv:improv /build/app.jar app.jar

USER improv
EXPOSE 8080

# Container-aware heap sizing; override JAVA_OPTS to tune.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Actuator health is the single source of truth for "is this container up".
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
