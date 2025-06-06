FROM gradle:jdk17 as builder

COPY ./ /app
WORKDIR /app
RUN gradle build


FROM amazoncorretto:17
COPY --from=builder /app/build/libs/product-0.0.1.jar /product/server.jar
WORKDIR /product
ENV PORT=8081
ENV REGISTER_WORKFLOW=false
ENV LHC_API_HOST=host
ENV LHC_API_PORT=2023

ENTRYPOINT [ "java", "-jar", "server.jar"]
