FROM gradle:jdk17 as builder

COPY ./ /api
WORKDIR /api
RUN gradle build

FROM amazoncorretto:17
COPY --from=builder /api/app/build/libs/app.jar /api/app.jar
WORKDIR /api

ENTRYPOINT [ "java", "-jar", "app.jar"]
