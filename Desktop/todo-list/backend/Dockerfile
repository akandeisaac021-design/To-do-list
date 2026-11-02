# Stage 1: build the jar. This stage's image (with the full JDK and Gradle
# cache) never ships - only its output (the jar) gets copied into stage 2.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy the wrapper and build files first, separately from src/, so Docker's
# layer cache can skip re-downloading Gradle itself on every rebuild when
# only application code (not build.gradle) has changed.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src
RUN ./gradlew build -x test --no-daemon

# Stage 2: the actual image Render runs. JRE only, not the full JDK -
# nothing here needs to compile anything, just run the already-built jar.
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
