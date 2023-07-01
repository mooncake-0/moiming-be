FROM openjdk:17

COPY moiming.jar ./moiming.jar
ENTRYPOINT ["java", "-jar", "moiming.jar"]

# docker buildx build --platform linux/arm64 --push -t 0612sha/moim:arm64.v5 .