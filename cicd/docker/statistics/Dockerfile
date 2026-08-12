FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY backend/statistics/build/libs/statistics.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]