# Build stage
FROM --platform=linux/amd64 gradle:8.5-jdk21 AS builder
WORKDIR /build

# 의존성 캐싱을 위한 그래들 파일만 먼저 복사
COPY build.gradle settings.gradle /build/
COPY gradle /build/gradle
COPY gradlew /build/
RUN chmod +x ./gradlew

# 의존성 다운로드 (캐시 활용)
RUN ./gradlew dependencies

# 모든 소스 코드 복사
COPY src /build/src

# 설정 파일 확인
RUN ls -la /build/src/main/resources/
RUN echo "Config files content preview:"
RUN head -n 5 /build/src/main/resources/application.yml
RUN head -n 5 /build/src/main/resources/application-dev.yml

# 빌드
RUN ./gradlew clean build -x test --no-daemon

# 빌드된 jar 확인
RUN ls -la /build/build/libs/
# Production stage
# Production stage
FROM --platform=linux/amd64 eclipse-temurin:21-jdk-alpine
WORKDIR /app

# 기본 시간대 설정
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 애플리케이션 jar 파일 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 서버 환경 설정
ENV SERVER_PORT=30010
ENV JAVA_OPTS="-XX:+UseZGC \
               -XX:+ZGenerational \
               -XX:+UseStringDeduplication \
               -XX:MaxRAMPercentage=75 \
               -XX:MaxMetaspaceSize=256m \
               -XX:+HeapDumpOnOutOfMemoryError \
               -Dfile.encoding=UTF-8 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=dev"

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# 비root 사용자 추가
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 컨테이너 실행
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]

EXPOSE ${SERVER_PORT}