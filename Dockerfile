# Build stage
FROM --platform=linux/amd64 gradle:8.5-jdk21 AS builder
WORKDIR /build

# 그래들 파일들 복사
COPY build.gradle settings.gradle gradlew /build/
COPY gradle /build/gradle

# 소스 복사
COPY src /build/src

# 권한 설정
RUN chmod +x ./gradlew

# 프로젝트 빌드 (프로덕션 프로파일로 빌드)
RUN ./gradlew clean build -x test

# Production stage
FROM --platform=linux/amd64 eclipse-temurin:21-jdk-alpine
WORKDIR /app

# 기본 시간대 설정
RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

# 알파인 리눅스 기본 설정
RUN apk add --no-cache curl

# 애플리케이션 jar 파일 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 서버 환경 설정
ENV SERVER_PORT=30010
ENV JAVA_OPTS="-XX:+UseZGC \
               -XX:+ZGenerational \
               -XX:+UseStringDeduplication \
               -XX:MaxRAMPercentage=75 \
               -Dfile.encoding=UTF-8 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=dev"

# 헬스체크를 위한 스크립트
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# 컨테이너 실행 시 실행할 명령어
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]

# API 서버 포트
EXPOSE ${SERVER_PORT}