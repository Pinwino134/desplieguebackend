FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY discoveryserver/pom.xml ./discoveryserver/pom.xml
COPY mvnw ./mvnw
COPY .mvn .mvn

RUN chmod +x mvnw

COPY discoveryserver/src ./discoveryserver/src

RUN ./mvnw -q -B -pl discoveryserver -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /workspace/discoveryserver/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]