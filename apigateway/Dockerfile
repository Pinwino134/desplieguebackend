FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY apigateway/pom.xml ./apigateway/pom.xml
COPY mvnw ./mvnw
COPY .mvn .mvn

RUN chmod +x mvnw

COPY apigateway/src ./apigateway/src

RUN ./mvnw -q -B -pl apigateway -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /workspace/apigateway/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]