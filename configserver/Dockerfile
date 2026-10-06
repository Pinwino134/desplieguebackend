FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY configserver/pom.xml ./configserver/pom.xml
COPY mvnw ./mvnw
COPY .mvn .mvn

RUN chmod +x mvnw

COPY configserver/src ./configserver/src

RUN ./mvnw -q -B -pl configserver -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /workspace/configserver/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]