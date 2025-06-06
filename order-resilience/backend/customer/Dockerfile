FROM gradle:jdk17 as builder

COPY ./ /app
WORKDIR /app
RUN gradle build


FROM amazoncorretto:17
COPY --from=builder /app/build/libs/customer-0.0.1.jar /customer/server.jar
WORKDIR /customer
ENV PORT=8080
ENV REGISTER_WORKFLOW=false
ENV LHC_API_HOST=host
ENV LHC_API_PORT=2023

ENTRYPOINT [ "java", "-jar", "server.jar"]
