# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /lh

# Copy the application JAR file to the container
COPY app/build/libs/app.jar /lh/myapp.jar

# Specify the command to run the application
CMD ["java", "-jar", "/lh/myapp.jar"]