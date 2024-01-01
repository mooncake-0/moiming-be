#!/bin/bash
# cd-start 는 cd 중 deploy 과정에서 실행중인 app 이 있는지 점검하고, 있다면 제거한다

ROOT_PATH="/home/ubuntu/dev-moiming-spring"
JAR="$ROOT_PATH/application.jar"
STOP_LOG="$ROOT_PATH/stop.log"
SERVICE_PID=$(pgrep -f $JAR) # 실행중인 Spring 서버의 PID

if [ -z "$SERVICE_PID" ]; then
  echo "서비스 NouFound" >> $STOP_LOG
else
  echo "서비스 종료 " >> $STOP_LOG
  kill "$SERVICE_PID"
  # kill -9 $SERVICE_PID # 강제 종료를 하고 싶다면 이 명령어 사용
fi