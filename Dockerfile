# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the application code
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Define environment variables if needed, e.g., for Spring profiles
# ENV SPRING_PROFILES_ACTIVE=prod

# Run the JAR file
ENTRYPOINT ["java", "-jar", "target/somali-ai-agent-0.0.1-SNAPSHOT.jar"]

# Note: Replace 'somali-ai-agent-0.0.1-SNAPSHOT.jar' with the actual name of your application JAR file if it's different.
# You can find the JAR file name in the 'target' directory after running 'mvn package'.
# This Dockerfile assumes you are using Maven and your Spring Boot application packages as a JAR.
# It also assumes the application runs on port 8080. If it's different, change the EXPOSE instruction.
