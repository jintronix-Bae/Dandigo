# ===== 1단계: 빌드 (JDK 21) =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 의존성 먼저 받아서 캐시 (소스만 바뀌면 이 단계는 재사용)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
# Windows에서 만든 gradlew의 줄바꿈(CRLF)을 고치고 실행 권한을 준다
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew --no-daemon dependencies > /dev/null

COPY src src
# 실행용 jar만 app.jar로 복사 (-plain.jar 제외)
RUN ./gradlew --no-daemon bootJar -x test \
    && find build/libs -name "*.jar" ! -name "*-plain.jar" -exec cp {} app.jar \;

# ===== 2단계: 실행 (JRE 21) =====
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul

# root가 아닌 사용자로 실행
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /workspace/app.jar app.jar
USER spring

EXPOSE 8080
# JAVA_OPTS로 메모리 등 JVM 옵션을 넘길 수 있다 (예: -Xmx512m)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
