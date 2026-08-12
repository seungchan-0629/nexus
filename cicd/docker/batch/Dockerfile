FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY backend/batch/build/libs/batch-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
