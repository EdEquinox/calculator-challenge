# Uses Eclipse Temurin JRE 17 base image
FROM eclipse-temurin:17-jre-focal

# Specifies the JAR file built by Gradle
ARG JAR_FILE=build/libs/rest.jar

# Copies the JAR file into the container
COPY ${JAR_FILE} app.jar

# Exposes port 8080 for the application
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]