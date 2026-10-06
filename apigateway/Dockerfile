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

WORKDIR /app

COPY --from=build /workspace/apigateway/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
