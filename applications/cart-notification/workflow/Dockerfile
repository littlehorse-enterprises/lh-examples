FROM gradle:jdk17 as builder

COPY ./ /app
WORKDIR /app
RUN gradle build

FROM amazoncorretto:17
COPY --from=builder /app/build/libs/workflow-0.0.1.jar /workflow/server.jar
WORKDIR /workflow
ENV PORT=8081
ENV LHC_API_HOST=host
ENV LHC_API_PORT=2023

ENTRYPOINT [ "java", "-jar", "server.jar"]
