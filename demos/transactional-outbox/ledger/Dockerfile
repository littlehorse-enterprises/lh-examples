FROM gradle:jdk17 as builder

COPY ./ /app
WORKDIR /app
RUN gradle build


FROM amazoncorretto:17
COPY --from=builder /app/build/libs/ledger-0.0.1.jar /ledger/server.jar
WORKDIR /ledger
ENV PORT=8081
ENV POSTGRES_DB=ledger
ENV POSTGRES_HOSTNAME=host
ENV POSTGRES_PORT=5432
ENV POSTGRES_USERNAME=postgres
ENV POSTGRES_PASSWORD=123456
ENV REGISTER_WORKFLOW=false
ENV WORKER=warehouse
ENV LHC_API_HOST=host
ENV LHC_API_PORT=2023

ENTRYPOINT [ "java", "-jar", "server.jar"]
