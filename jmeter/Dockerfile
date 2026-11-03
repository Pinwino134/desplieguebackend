# PLAN B: usar un JMeter descargado a mano (si los servidores de Apache fallan).
#
# 1. Descarga en tu navegador:
#    https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
# 2. Guarda el archivo dentro de la carpeta jmeter/ (junto a este Dockerfile.local).
# 3. Renombra este archivo a "Dockerfile" (reemplaza el que ya existe).
# 4. Ejecuta: docker compose --profile loadtest build jmeter
FROM eclipse-temurin:21-jre-alpine

ARG JMETER_VERSION=5.6.3

COPY apache-jmeter-${JMETER_VERSION}.tgz /tmp/jmeter.tgz

RUN tar xzf /tmp/jmeter.tgz -C /opt \
 && rm -f /tmp/jmeter.tgz \
 && ln -s /opt/apache-jmeter-${JMETER_VERSION}/bin/jmeter /usr/local/bin/jmeter

WORKDIR /test
ENTRYPOINT ["jmeter"]
