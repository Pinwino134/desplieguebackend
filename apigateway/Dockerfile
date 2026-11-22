FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

# POM padre
COPY pom.xml ./pom.xml

# POM de API Gateway
COPY apigateway/pom.xml ./apigateway/pom.xml

# Maven Wrapper
COPY apigateway/mvnw ./mvnw
COPY apigateway/.mvn .mvn

RUN chmod +x mvnw

# Código fuente de API Gateway
COPY apigateway/src ./apigateway/src

# Construir API Gateway
RUN cd apigateway && ../mvnw -q -B clean package -DskipTests


FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/apigateway/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--XX:MaxRAMPercentage=75.0} -jar app.jar"]
