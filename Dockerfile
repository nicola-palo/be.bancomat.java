# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY . .

# Build a runnable jar
RUN ./mvnw -q -DskipTests package


# --- Run stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the fat jar
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
