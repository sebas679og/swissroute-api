FROM maven:3.9.9-eclipse-temurin-21 AS builder

ARG UID=1010
ARG GID=1010

RUN groupadd -g "${GID}" group4 \
    && useradd --create-home --no-log-init -u "${UID}" -g group4 group4

WORKDIR /opt/app

COPY --chown=group4:group4 pom.xml .
RUN mvn dependency:go-offline -q

COPY --chown=group4:group4 src ./src

RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre AS runtime

ARG BUILD_DATE=unknown
ARG BUILD_VERSION=unknown
ARG IMAGE_DESCRIPTION=unknown
ARG IMAGE_NAME="Swiss Route API"
ARG UID=1010
ARG GID=1010

LABEL group4.sports-pulse.build-date=$BUILD_DATE \
      group4.sports-pulse.name=$IMAGE_NAME \
      group4.sports-pulse.description=$IMAGE_DESCRIPTION \
      group4.sports-pulse.base.image="eclipse-temurin:21-jre" \
      group4.sports-pulse.version=$BUILD_VERSION \
      maintainer="group4"

RUN groupadd -g "${GID}" group4 \
    && useradd --create-home --no-log-init -u "${UID}" -g group4 group4

WORKDIR /opt/app

COPY --from=builder --chown=group4:group4 /opt/app/target/swissroute-api.jar app.jar

USER group4

ENTRYPOINT ["java","-jar","app.jar"]