#!/bin/bash

pushd ..

gradle clean
gradle build -x test

docker buildx build --platform linux/arm64 --push -t 0612sha/moim-arm64:latest .
rm moiming.jar


