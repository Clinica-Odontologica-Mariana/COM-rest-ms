# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Gradle metadata first to improve Docker layer caching.
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Copy source only after dependencies and wrapper are in place.
COPY src ./src

# Build the executable Spring Boot jar.
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copy only the Spring Boot executable jar (ignore the plain jar when present).
RUN set -eux; \
	groupadd --system spring; \
	useradd --system --gid spring --create-home spring

COPY --from=builder /app/build/libs/rest-ms-*.jar /tmp/
RUN set -eux; \
	BOOT_JAR="$(find /tmp -maxdepth 1 -type f -name '*.jar' | grep -v -- '-plain\.jar$' | head -n 1)"; \
	test -n "$BOOT_JAR"; \
	mv "$BOOT_JAR" /app/app.jar; \
	rm -f /tmp/*.jar; \
	chown spring:spring /app/app.jar

EXPOSE 8080

USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
