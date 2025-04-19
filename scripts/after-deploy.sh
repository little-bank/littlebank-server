#!/bin/bash
REPOSITORY=/home/ubuntu/

cd $REPOSITORY/littlebank

if [ "$DEPLOYMENT_GROUP_NAME" = "env-dev" ]; then
  echo "> Stop & Remove docker services. (dev)"
  docker compose -f docker-compose.dev.yml down

  echo "> Run new docker services. (dev)"
  docker compose -f docker-compose.dev.yml up --build -d

elif [ "$DEPLOYMENT_GROUP_NAME" = "env-prod" ]; then
  echo "> Stop & Remove docker services. (prod)"
  docker compose -f docker-compose.prod.yml down

  echo "> Run new docker services. (prod)"
  docker compose -f docker-compose.prod.yml up --build -d
fi
