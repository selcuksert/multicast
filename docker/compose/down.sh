PSScriptRoot=$(dirname "$0")
NETWORK_NAME="netipvlan"

docker-compose -f "$PSScriptRoot"/docker-compose.yml down

if [ -d "$PSScriptRoot"/logs ]
then
  rm -rf "$PSScriptRoot"/logs
fi

if docker network inspect $NETWORK_NAME &>/dev/null
then
  echo "Removing network"
  docker network rm $NETWORK_NAME
fi