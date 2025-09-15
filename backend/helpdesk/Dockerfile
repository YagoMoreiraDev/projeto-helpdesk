# ====== STAGE 1: build ======
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia apenas arquivos de configuração primeiro (cache mais eficiente)
COPY pom.xml ./
COPY .mvn .mvn
RUN mvn -q -DskipTests dependency:go-offline

# Copia o código e builda
COPY src ./src
RUN mvn -q -DskipTests package

# ====== STAGE 2: runtime ======
FROM eclipse-temurin:21-jre

# Usuário não-root
RUN useradd -ms /bin/bash appuser
WORKDIR /app

# Copia o JAR do build
COPY --from=build /app/target/*.jar /app/app.jar

# Variáveis padrão (podem ser sobrescritas no run/compose)
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=7013 \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    TZ=America/Sao_Paulo

EXPOSE 7013

# Healthcheck bate no ping que você já tem no backend (ajuste se necessário)
HEALTHCHECK --interval=20s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:7013/api/chamados/ping || exit 1

USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]
