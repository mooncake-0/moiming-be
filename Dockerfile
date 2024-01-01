# Dockerfile

# JDK11 Image BASE
FROM        openjdk:11

ARG         JAR_FILE=build/libs/*.jar

ENV         PROFILE=dev \
            DEV_DB_HOST=dev-moiming-db-container \
            DEV_DB_PORT=3306 \
            DEV_SCHEMA_NAME=dev-moiming-db \
            DEV_DB_USERNAME=dev-app \
            DEV_DB_PASSWORD=dev-app

#JAR 복제
COPY        ${JAR_FILE} dev-moiming-app.jar

# 실행 명령어
ENTRYPOINT  ["java", "-jar", "dev-moiming-app.jar"]