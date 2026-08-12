FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY backend/billing-batch/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
