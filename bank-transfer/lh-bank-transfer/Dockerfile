# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /lh

# Copy the application JAR file to the container
COPY app/build/libs/app.jar /lh/app.jar
COPY scripts/run-all-taskWorkers-docker.sh /lh/run-all-taskWorkers.sh
COPY scripts/register-workflow-docker.sh /lh/register-workflow.sh

# Specify the command to run the application
CMD ["/bin/sh", "/lh/run-all-taskWorkers.sh"]
