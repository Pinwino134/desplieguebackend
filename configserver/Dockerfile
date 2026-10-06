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

# Maven Wrapper de Config Server
COPY configserver/mvnw ./mvnw
COPY configserver/.mvn .mvn

RUN chmod +x mvnw

# Código fuente
COPY configserver/src ./configserver/src

# Construir solamente Config Server
RUN ./mvnw -q -B -pl configserver clean package -DskipTests


FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /workspace/configserver/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
