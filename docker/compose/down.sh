PSScriptRoot=$(dirname "$0")

docker-compose -f "$PSScriptRoot"/docker-compose.yml down

if [ -d "$PSScriptRoot" ]
then
  rm -rf "$PSScriptRoot"/logs/*
fi