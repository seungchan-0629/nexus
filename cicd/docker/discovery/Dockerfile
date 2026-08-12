FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY backend/discovery/build/libs/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
