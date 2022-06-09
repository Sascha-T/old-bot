FROM gradle:jdk17 as build

RUN mkdir /build

COPY bot /build/bot
COPY modules /build/modules

COPY build.gradle /build/build.gradle
COPY settings.gradle /build/settings.gradle

WORKDIR /build
RUN gradle build

FROM openjdk:17

RUN mkdir -p /app/bin
RUN mkdir -p /app/image-modules
VOLUME ["/app/data"]

WORKDIR /app/data
ENV SBOT_FORCE_LOADED_MODULES=/app/image-modules

COPY --from=build /build/bot/build/libs/bot*-all.jar /app/bin/bot.jar
COPY --from=build /build/build/modules/*-all.jar /app/image-modules/

ENTRYPOINT ["java", "-jar", "/app/bin/bot.jar"]
