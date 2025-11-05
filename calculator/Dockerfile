# Uses Eclipse Temurin JRE 17 base image
FROM eclipse-temurin:17-jre-focal

# Specifies the JAR file built by Gradle
ARG JAR_FILE=build/libs/calculator.jar

# Copies the JAR file from the build directory on the host to the container
COPY ${JAR_FILE} app.jar

# Defines the port that the Spring Boot application will expose (8081 for the Calculator)
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app.jar"]