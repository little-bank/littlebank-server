#!/bin/bash
REPOSITORY=/home/ubuntu/

cd $REPOSITORY/littlebank

if [ -f "docker-compose.dev.yml" ]; then
  echo "> Stop & Remove docker services. (dev)"
  cd ..
  docker compose -f docker-compose.dev.yml down
else
  echo "> Stop & Remove docker services. (prod)"
  cd ..
  docker compose -f docker-compose.prod.yml down
fi

if [ -f "docker-compose.dev.yml" ]; then
  echo "> Run new docker services. (dev)"
  docker compose -f docker-compose.dev.yml up --build -d
else
  echo "> Run new docker services. (prod)"
  docker compose -f docker-compose.prod.yml up --build -d
fi