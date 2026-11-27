# ---------- Etapa 1: compilar con Maven ----------
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY microservices/pom.xml ./microservices/pom.xml
COPY microservices/productosservice/pom.xml ./microservices/productosservice/pom.xml
COPY microservices/productosservice/mvnw ./microservices/productosservice/mvnw
COPY microservices/productosservice/.mvn ./microservices/productosservice/.mvn

WORKDIR /workspace/microservices/productosservice
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline || true

WORKDIR /workspace
COPY microservices/productosservice/src ./microservices/productosservice/src

WORKDIR /workspace/microservices/productosservice
RUN ./mvnw -q -B clean package -DskipTests

# ---------- Etapa 2: imagen final endurecida ----------
FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/microservices/productosservice/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8095
HEALTHCHECK --interval=20s --timeout=5s --start-period=45s --retries=5 \
  CMD curl -fsS http://localhost:8095/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--XX:MaxRAMPercentage=75.0} -jar app.jar"]
