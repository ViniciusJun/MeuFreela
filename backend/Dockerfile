# ============================================================
# STAGE 1 — BUILD
# ============================================================
# Compila o JAR usando Maven + JDK 21.
# Imagem pesada (~500MB), mas descartada no final.
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia apenas o pom.xml primeiro — aproveita cache do Docker
# quando só o código-fonte muda (dependências já baixadas).
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código e compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# STAGE 2 — RUNTIME
# ============================================================
# Imagem final: apenas JRE (não JDK) + Alpine (leve).
FROM eclipse-temurin:21-jre-alpine

# Boa prática: rodar como usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copia apenas o JAR do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Health check nativo do Docker (usa o Actuator que acabamos de configurar)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Porta exposta
EXPOSE 8080

# Variáveis de ambiente padrão (sobrescritas por docker-compose)
ENV SPRING_PROFILES_ACTIVE=postgres
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Entrypoint com JAVA_OPTS configurável
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]