FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

# POM raíz
COPY pom.xml ./pom.xml

# POMs de los módulos principales
COPY configserver/pom.xml ./configserver/pom.xml
COPY discoveryserver/pom.xml ./discoveryserver/pom.xml
COPY microservices/pom.xml ./microservices/pom.xml

# POMs de los microservicios
COPY microservices/productosservice/pom.xml ./microservices/productosservice/pom.xml
COPY microservices/ventasservice/pom.xml ./microservices/ventasservice/pom.xml
COPY microservices/authservice/pom.xml ./microservices/authservice/pom.xml
COPY microservices/gerenciaservice/pom.xml ./microservices/gerenciaservice/pom.xml
COPY microservices/logisticaservice/pom.xml ./microservices/logisticaservice/pom.xml
COPY microservices/pagosservice/pom.xml ./microservices/pagosservice/pom.xml
COPY microservices/serviciosservice/pom.xml ./microservices/serviciosservice/pom.xml

# Maven Wrapper de Discovery Server
COPY discoveryserver/mvnw ./mvnw
COPY discoveryserver/.mvn .mvn

RUN chmod +x mvnw

# Código de Discovery Server
COPY discoveryserver/src ./discoveryserver/src

# Construir solamente Discovery Server
RUN ./mvnw -q -B -pl discoveryserver clean package -DskipTests


FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /workspace/discoveryserver/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8761
HEALTHCHECK --interval=15s --timeout=5s --start-period=35s --retries=5 \
  CMD curl -fsS http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--XX:MaxRAMPercentage=75.0} -jar app.jar"]
