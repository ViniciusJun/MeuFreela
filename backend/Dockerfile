# ============================================================
# STAGE 1 — BUILD
# ============================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia apenas o pom.xml primeiro — aproveita cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código e compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# STAGE 2 — RUNTIME
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# Boa prática: rodar como usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copia apenas o JAR do stage anterior
COPY --from=build /app/target/*.jar app.jar

# CORREÇÃO: Cria a pasta e altera o dono ANTES de mudar para o usuário spring
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Agora sim, muda para o usuário sem privilégios
USER spring:spring

# Health check nativo do Docker
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Porta exposta
EXPOSE 8080

# Variáveis de ambiente padrão
ENV SPRING_PROFILES_ACTIVE=postgres
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Entrypoint com JAVA_OPTS configurável
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]