FROM gradle:jdk17 as builder

COPY ./ /app
WORKDIR /app
RUN gradle build


FROM amazoncorretto:17
COPY --from=builder /app/build/libs/verification-0.0.1.jar /verification/server.jar
WORKDIR /verification
ENV PORT=8080
ENV POSTGRES_DB=verifications
ENV POSTGRES_HOSTNAME=host
ENV POSTGRES_PORT=5432
ENV POSTGRES_USERNAME=postgres
ENV POSTGRES_PASSWORD=123456
ENV REGISTER_WORKFLOW=false
ENV LHC_API_HOST=host
ENV LHC_API_PORT=2023

ENTRYPOINT [ "java", "-jar", "server.jar"]
