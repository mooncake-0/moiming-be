# Dockerfile

# JDK11 Image BASE
FROM        openjdk:11

ARG         JAR_FILE=build/libs/*.jar
ARG         DEV_DB_PASSWORD
ARG         COOL_SMS_API_KEY
ARG         COOL_SMS_SECRET_KEY
ARG         AWS_DEV_S3_BUCKET_NAME
ARG         AWS_IAM_ACCESS_KEY
ARG         AWS_IAM_SECRET_ACCESS_KEY


ENV         PROFILE=dev \
            DEV_DB_HOST=dev-moiming-db-container \
            DEV_DB_PORT=3306 \
            DEV_DB_SCHEMA_NAME=dev-moiming-db \
            DEV_DB_USERNAME=dev-app \
            DEV_DB_PASSWORD=${DEV_DB_PASSWORD} \
            COOL_SMS_API_KEY=${COOL_SMS_API_KEY} \
            COOL_SMS_SECRET_KEY=${COOL_SMS_SECRET_KEY} \
            AWS_DEV_S3_BUCKET_NAME = ${AWS_DEV_S3_BUCKET_NAME} \
            AWS_IAM_ACCESS_KEY = ${AWS_IAM_ACCESS_KEY} \
            AWS_IAM_SECRET_ACCESS_KEY = ${AWS_IAM_SECRET_ACCESS_KEY}


#JAR 복제
COPY        ${JAR_FILE} dev-moiming-app.jar

# 실행 명령어
ENTRYPOINT  ["java", "-jar", "dev-moiming-app.jar"]