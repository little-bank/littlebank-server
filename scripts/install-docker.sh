#!/bin/bash

# Docker 설치 여부 확인
if ! command -v docker &> /dev/null; then
  echo "[INFO] Installing Docker..."
  apt update
  apt install -y docker.io
  systemctl start docker
  systemctl enable docker
  usermod -aG docker ubuntu
else
  echo "[INFO] Docker already installed. Skipping..."
fi

# Docker Compose v2 설치 여부 확인
if ! docker compose version &> /dev/null; then
  echo "[INFO] Installing Docker Compose v2..."
  DOCKER_CONFIG=${DOCKER_CONFIG:-/usr/lib/docker}
  mkdir -p $DOCKER_CONFIG/cli-plugins
  curl -SL https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-linux-x86_64 \
    -o $DOCKER_CONFIG/cli-plugins/docker-compose
  chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose
else
  echo "[INFO] Docker Compose already installed. Skipping..."
fi
