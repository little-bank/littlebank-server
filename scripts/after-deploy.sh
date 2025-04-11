#!/bin/bash

REPOSITORY=/home/ubuntu/
cd $REPOSITORY/littlebank

YML="docker-compose.prod.yml"
if [ -f "docker-compose.dev.yml" ]; then
  YML="docker-compose.dev.yml"
fi

echo "> Checking running containers..."
if docker compose -f $YML ps | grep Up &> /dev/null; then
  echo "> Stopping running docker services..."
  docker compose -f $YML down
fi

echo "> Building docker images if needed..."
docker compose -f $YML build

echo "> Starting new docker services..."
docker compose -f $YML up -d
