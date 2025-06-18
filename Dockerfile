# 베이스 이미지 설정
FROM amazoncorretto:17 AS builder

# 시간대 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Gradle 및 소스 코드 복사
WORKDIR /app
COPY . .

# Gradle 빌드 실행 (테스트 제외)
RUN ./gradlew clean build -x test

# 최종 이미지
FROM amazoncorretto:17

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 시간대 동기화
ENV TZ=Asia/Seoul

# 실행 명령 수정 - profile은 ENTRYPOINT에서 인자로 받음
ENTRYPOINT ["java", "-jar", "app.jar"]
