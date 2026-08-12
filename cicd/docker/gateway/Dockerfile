FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY backend/gateway/build/libs/*.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
