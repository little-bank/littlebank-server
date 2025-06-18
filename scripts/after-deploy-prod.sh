#!/bin/bash

REPO=/home/ubuntu/littlebank
cd $REPO

IMAGE_NAME=littlebank-prod
CONTAINER_NAME=littlebank-prod-container
PROFILE=prod
PORT=8080

echo "> Stop & remove old container"
docker stop $CONTAINER_NAME 2>/dev/null || true
docker rm $CONTAINER_NAME 2>/dev/null || true

echo "> Remove old image"
docker rmi $IMAGE_NAME:latest 2>/dev/null || true

echo "> Build Docker image"
docker build -t $IMAGE_NAME .

echo "> Run new container with profile: $PROFILE"
docker run -d \
  --name $CONTAINER_NAME \
  -e SPRING_PROFILES_ACTIVE=$PROFILE \
  -v /etc/localtime:/etc/localtime:ro \
  -p $PORT:8080 \
  $IMAGE_NAME
