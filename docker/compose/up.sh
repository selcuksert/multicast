#!/bin/zsh

PSScriptRoot=$(dirname "$0")

docker-compose -f "$PSScriptRoot"/docker-compose.yml up -d --scale receiver=3 --scale sender=1