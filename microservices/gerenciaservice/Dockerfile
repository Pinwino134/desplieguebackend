# ---------- Etapa 1: compilar con Maven ----------
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY microservices/pom.xml ./microservices/pom.xml
COPY microservices/gerenciaservice/pom.xml ./microservices/gerenciaservice/pom.xml
COPY microservices/gerenciaservice/mvnw ./microservices/gerenciaservice/mvnw
COPY microservices/gerenciaservice/.mvn ./microservices/gerenciaservice/.mvn

WORKDIR /workspace/microservices/gerenciaservice
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline || true

WORKDIR /workspace
COPY microservices/gerenciaservice/src ./microservices/gerenciaservice/src

WORKDIR /workspace/microservices/gerenciaservice
RUN ./mvnw -q -B clean package -DskipTests

# ---------- Etapa 2: imagen final endurecida ----------
FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/microservices/gerenciaservice/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8092
HEALTHCHECK --interval=20s --timeout=5s --start-period=45s --retries=5 \
  CMD curl -fsS http://localhost:8092/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--XX:MaxRAMPercentage=75.0} -jar app.jar"]
