#!/bin/sh

if [ $# -eq 0 ]; then
   echo "Usage: $0 tag"
   exit 1
fi

TAG=$1

docker build --tag bonigarcia/webdrivermanager:$TAG --build-arg VERSION=$TAG .
docker login -u=bonigarcia
docker push bonigarcia/webdrivermanager:$TAG
