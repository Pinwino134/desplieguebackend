# ============================================================
# Dockerfile genérico para los microservicios del reactor
# (authservice, productosservice, ventasservice, pagosservice,
#  logisticaservice, gerenciaservice).
#
# IMPORTANTE: el "context" de build en docker-compose.yml debe
# ser la RAÍZ del repo (eccomerce/), no esta carpeta, porque
# este módulo depende de ../pom.xml y del ../../pom.xml raíz
# para resolver versiones (herencia de Maven).
# ============================================================

# ---------- Etapa 1: compilar con Maven ----------
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

# Copiamos primero los POM (padre, módulo padre y el del servicio)
# para aprovechar el cache de capas de Docker: si solo cambia el
# código fuente, Maven no vuelve a descargar todas las dependencias.
COPY pom.xml ./pom.xml
COPY microservices/pom.xml ./microservices/pom.xml
COPY microservices/productosservice/pom.xml ./microservices/productosservice/pom.xml
COPY microservices/productosservice/mvnw ./microservices/productosservice/mvnw
COPY microservices/productosservice/.mvn ./microservices/productosservice/.mvn

WORKDIR /workspace/microservices/productosservice
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline || true

# Ahora sí el código fuente
WORKDIR /workspace
COPY microservices/productosservice/src ./microservices/productosservice/src

WORKDIR /workspace/microservices/productosservice
RUN ./mvnw -q -B clean package -DskipTests

# ---------- Etapa 2: imagen final, solo con el JRE ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /workspace/microservices/productosservice/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
