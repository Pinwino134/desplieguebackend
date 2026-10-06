# Proyecto independiente (su propio pom.xml raíz), el contexto de build
# es esta misma carpeta.

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline || true

COPY src ./src
RUN ./mvnw -q -B clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
