# ====== STAGE 1: build ======
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# ====== STAGE 2: runtime ======
FROM eclipse-temurin:21-jre

# Instala curl para o healthcheck
USER root
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Usuário não-root
RUN useradd -ms /bin/bash appuser
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=7013 \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    TZ=America/Sao_Paulo

EXPOSE 7013

HEALTHCHECK --interval=20s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:7013/api/chamados/ping || exit 1

USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]
