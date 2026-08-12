FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache nodejs npm

WORKDIR /app

COPY mcp/naver-news-search /app/mcp
RUN cd /app/mcp && npm install --omit=dev

COPY backend/monolith/build/libs/monolith.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]