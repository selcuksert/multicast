#!/bin/zsh

PSScriptRoot=$(dirname "$0")

docker run --rm -it \
-v "$PSScriptRoot"/config:/opt/udp/receiver/config \
-v "$PSScriptRoot"/logs:/opt/udp/receiver/logs \
-e RECEIVER_ID=$(($RANDOM % 5 + 1)) \
--net ipvnet \
custom/multicast/message-receiver