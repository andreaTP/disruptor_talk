#!/usr/bin/env bash

case "$1" in
  build)
    docker -D build -t="slide-docker:latest" .
  ;;
  run)
    #remember to start nvidia-docker-plugin
    docker run --rm -i -t -p 4000:4000 -p 8000:8000 "slide-docker:latest"
  ;;
  *)
    echo "Usage [ build, run ] "
    exit 0
  ;;
esac
