#!/bin/zsh

PSScriptRoot=$(dirname "$0")
NETWORK_NAME="netipvlan"

if ! docker network inspect $NETWORK_NAME &>/dev/null
then
  echo "Creating network"
  docker network create -d ipvlan -o parent=eth0 \
  -o ipvlan_mode=l2 \
  --subnet=192.168.1.0/24 \
  --gateway=192.168.1.1 \
  --ip-range=192.168.1.192/27 $NETWORK_NAME
fi
docker-compose -f "$PSScriptRoot"/docker-compose.yml up -d --scale receiver=3 --scale sender=1