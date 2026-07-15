# --- 1단계: 빌드 ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 캐시를 위해 Gradle 관련 파일부터 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# 소스 복사 후 빌드 (테스트는 배포 이미지 빌드 단계에서 생략)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# --- 2단계: 실행 ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Railway/Render 등이 주입하는 PORT 환경변수를 애플리케이션이 사용한다 (application.yml 참고).
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
