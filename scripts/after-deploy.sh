#!/bin/bash
REPOSITORY=/home/ubuntu/

cd $REPOSITORY/littlebank

if [ -f "docker-compose.dev.yml" ]; then
  echo "> Stop & Remove docker services. (dev)"
  docker compose -f docker-compose.dev.yml down

  echo "> Run new docker services. (dev)"
    docker compose -f docker-compose.dev.yml up --build -d
else
  echo "> Stop & Remove docker services. (prod)"
  docker compose -f docker-compose.prod.yml down

  echo "> Run new docker services. (prod)"
  docker compose -f docker-compose.prod.yml up --build -d
fi
