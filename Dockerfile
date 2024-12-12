# Step 1: Base Image 선택
FROM openjdk:17-jdk-slim

# Step 2: JAR 파일 복사
COPY build/libs/my-review-service.jar app.jar

# Step 3: 실행 명령어 지정
ENTRYPOINT ["java", "-jar", "/app.jar"]
