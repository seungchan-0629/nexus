FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY backend/pos/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
