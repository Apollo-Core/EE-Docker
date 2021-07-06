FROM gradle:6.8.2-jdk11 as builder
WORKDIR /project
COPY . /project/
RUN gradle assemble --no-daemon

FROM adoptopenjdk:11-jre-hotspot
ENV APP_DIR /application
ENV APP_FILE EE-Docker.jar

ENV DOCKERIZED True

ARG HOST_CONNECTION_TYPE
ENV HOST_CONNECTION_TYPE=${HOST_CONNECTION_TYPE:-unix}
ENV DOCKERIZED_APPLICATION=${DOCKERIZED_APPLICATION:-unix}

EXPOSE 5055

WORKDIR $APP_DIR
COPY --from=builder /project/build/libs/*.jar $APP_DIR/$APP_FILE

ENTRYPOINT ["sh", "-c"]

CMD ["exec java -jar $APP_FILE"]
