#!/bin/bash


# imports
. ./scripts/envVar.sh
. ./scripts/configUpdate.sh

CHANNEL_NAME="$1"
## Orderer intended for removal from channels. Format sample: orderer2.example.com:7060
ORDERER_TO_REMOVE_ADDRESS="$2"

: ${DELAY:="3"}
: ${TIMEOUT:="10"}
: ${VERBOSE:="true"}
: ${ORDERER_TLS_HOSTNAME:="orderer.example.com"}
: ${ORDERER_PORT:="7050"}
: ${ORDERER_CA:="${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem"}

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=${PWD}/configtx

usg () {
	infoln "Usage: updateOrderers <channel_name> <orderer_to_remove_address>";
	infoln "Example: updateOrderers mychannel1 orderer2.example.com:7060";
	exit
}

removeOrdererFromJsonConfig () {
  configtxlator proto_decode --input ./channel-artifacts/${CHANNEL_NAME}.block --type common.Block | jq .data.data[0].payload.data.config > config.json
  cp config.json modified_config.json

  jq '.channel_group.values.OrdererAddresses.value.addresses -= ["'$ORDERER_TO_REMOVE_URL':$ORDERER_TO_REMOVE_PORT"]' modified_config.json >& cfgTmp.json
  for i in $(jq '.channel_group.groups.Orderer.values.ConsensusType.value.metadata.consenters | keys | .[]' config.json); 
  do
    hostName=$(echo $(jq '.channel_group.groups.Orderer.values.ConsensusType.value.metadata.consenters['${i}'].host' config.json))
    if [ $hostName = "\"${ORDERER_TO_REMOVE_URL}\"" ]; 
	then
      concenter=$(jq '.channel_group.groups.Orderer.values.ConsensusType.value.metadata.consenters['${i}']' config.json)
      jq '.channel_group.groups.Orderer.values.ConsensusType.value.metadata.consenters -= ['"${concenter}"']' cfgTmp.json >& modified_config.json
    fi
  done
}

removeOrderer () {
  infoln "About to remove orderer $ORDERER_TO_REMOVE_ADDRESS from configuration of channel $CHANNEL_NAME"; 
  
  removeOrdererFromJsonConfig

  infoln "Creating config transaction to add/remove orderers on channel"

  # Compute a config update, based on the differences between config.json and modified_config.json,
  # write it as a transaction to <channel_name>_update_orderers.pb
  createConfigUpdate ${CHANNEL_NAME} config.json modified_config.json ${CHANNEL_NAME}_update_orderers.pb

  infoln "Signing config transaction"
  export FABRIC_CFG_PATH=${PWD}/../config
  setGlobalsOrderer
  peer channel signconfigtx -f ${CHANNEL_NAME}_update_orderers.pb

  infoln "Submitting update transaction ..."
  peer channel update -f ${CHANNEL_NAME}_update_orderers.pb -c ${CHANNEL_NAME} -o ${ORDERER_TLS_HOSTNAME}:${ORDERER_PORT} --ordererTLSHostnameOverride ${ORDERER_TLS_HOSTNAME} --tls --cafile ${ORDERER_CA}
  { set +x; } 2>/dev/null

  successln "Config transaction update to network submitted !!!"
}

addOrderer () {
  infoln "About to add orderer $ORDERER_TO_REMOVE_ADDRESS to configuration of channel $CHANNEL_NAME"; 
  fatalln "Add orderer currently unsupported !!!"
}

cleanup () {
  unset CHANNEL_NAME
  unset ORDERER_TO_REMOVE_ADDRESS
  unset MODE
}



cleanup

# parse flags
while [[ $# -ge 1 ]] ; do
  key="$1"
  case $key in
  -h )
    usg
    ;;
  -rm )
    MODE="RM"
	ORDERER_TO_REMOVE_ADDRESS="$2"
    shift
    ;;
  -add )
    MODE="ADD"
	ORDERER_TO_REMOVE_ADDRESS="$2"
    shift
    ;;
  -c )
    CHANNEL_NAME="$2"
    shift
    ;;
  * )
    errorln "Unknown flag: $key"
    usg
    ;;
  esac
  shift
done

if [ -z "$CHANNEL_NAME" ];
then
	errorln "CHANNEL_NAME is undefined";
	usg
fi

if [ -z "$ORDERER_TO_REMOVE_ADDRESS" ]; 
then 
	errorln "ORDERER_TO_REMOVE_ADDRESS is undefined";
	usg
fi

ORDERER_TO_REMOVE_URL=$(echo $ORDERER_TO_REMOVE_ADDRESS | cut -d : -f 1)
ORDERER_TO_REMOVE_PORT=$(echo $ORDERER_TO_REMOVE_ADDRESS | cut -d : -f 2)

if [ "${MODE}" == "RM" ]; then
  removeOrderer
elif [ "${MODE}" == "ADD" ]; then
  addOrderer
else
  errorln "Unsupported mode $MODE"
  usg
fi
  

