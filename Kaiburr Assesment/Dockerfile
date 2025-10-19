# Multi-stage build: first stage for building the application
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy source code and build the application
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Second stage: runtime image with only the JAR file
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/kaiburr-task-manager-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV MONGODB_URI=mongodb://mongo:27017/kaiburr

ENTRYPOINT ["java","-jar","/app/app.jar"]


