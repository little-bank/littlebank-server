#!/bin/bash

REPOSITORY=/home/ubuntu/
cd $REPOSITORY/littlebank

# 사용할 YML 결정
YML="docker-compose.prod.yml"
if [ -f "docker-compose.dev.yml" ]; then
  YML="docker-compose.dev.yml"
fi

echo "> Checking running containers..."
if docker compose -f $YML ps | grep Up &> /dev/null; then
  echo "> Stopping running docker services..."
  docker compose -f $YML down
fi

echo "> Pulling images (if needed)..."
docker compose -f $YML pull

echo "> Building changed docker images only..."
docker compose -f $YML build --parallel --no-cache=false

echo "> Starting docker services..."
docker compose -f $YML up -d --remove-orphans
