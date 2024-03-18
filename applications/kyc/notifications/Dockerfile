FROM gradle:jdk17 as builder

COPY ./ /app
WORKDIR /app
RUN gradle build


FROM amazoncorretto:17
COPY --from=builder /app/build/libs/notifications-0.0.1.jar /notifications/server.jar
WORKDIR /notifications
ENV LHC_API_HOST=localhost
ENV LHC_API_PORT=2023

ENV FRONTEND_URL=http://localhost:3000
ENV SENDGRID_ENABLED=false
ENV SENDGRID_API_KEY=
ENV SENDGRID_SENDER=notifications@localhost
ENV MANAGER_EMAIL=manager@localhost
ENTRYPOINT [ "java", "-jar", "server.jar"]
