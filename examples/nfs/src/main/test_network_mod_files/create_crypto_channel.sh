#!/bin/bash

export PATH=${PWD}/../bin:$PATH
cp /build_temp/config/configtx.yaml ${PWD}/configtx/configtx.yaml
export FABRIC_CFG_PATH=${PWD}/configtx
export VERBOSE=false

. scripts/utils.sh

createChannelTx() {
	set -x
	configtxgen -profile $CHANNEL_PROFILE -outputCreateChannelTx ./channel-artifacts/${CHANNEL_NAME}.tx -channelID $CHANNEL_NAME
	res=$?
	{ set +x; } 2>/dev/null
  verifyResult $res "Failed to generate channel configuration transaction..."
}

verifyResult() {
  if [ $1 -ne 0 ]; then
    fatalln "$2"
  fi
}

if [ ! -d "channel-artifacts" ]; then
	mkdir channel-artifacts
fi

## Create channeltx
CHANNEL_NAME=$1
CHANNEL_PROFILE=$2
infoln "Generating channel create transaction '${CHANNEL_NAME}.tx'"
createChannelTx
