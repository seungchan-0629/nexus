# 공식 지원이 종료된 openjdk 대신 안정적이고 가벼운 eclipse-temurin 사용
FROM eclipse-temurin:17-jdk-alpine

# 컨테이너 내부의 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 파일을 컨테이너 내부로 복사
COPY build/libs/*.jar app.jar

# 유레카 서버 포트(9090) 노출
EXPOSE 9090

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]