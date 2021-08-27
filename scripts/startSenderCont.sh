#!/bin/zsh

PSScriptRoot=$(dirname "$0")

docker run --name multicast-sender --rm -it \
-v "$PSScriptRoot"/logs:/opt/udp/sender/logs \
-v "$PSScriptRoot"/config:/opt/udp/sender/config \
--net ipvnet \
custom/multicast/message-sender