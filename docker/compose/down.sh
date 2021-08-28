PSScriptRoot=$(dirname "$0")

docker-compose -f "$PSScriptRoot"/docker-compose.yml down

if [ -d "$PSScriptRoot"/logs ]
then
  rm -rf "$PSScriptRoot"/logs
fi