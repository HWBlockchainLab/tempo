#!/bin/bash

cd /fabric
echo "working dir is ${PWD}"

# imports  
. /scripts/envVar.sh
. /scripts/utils.sh

CHANNEL_NAME="$1"
DELAY="$2"
MAX_RETRY="$3"
VERBOSE="$4"
: ${CHANNEL_NAME:="mychannel"}
: ${DELAY:="3"}
: ${MAX_RETRY:="5"}
: ${VERBOSE:="false"}

createChannel() {
  ORG=$1
  PEER_NUM=$2
	setGlobals $ORG $PEER_NUM
	# Poll in case the raft leader is not set yet
	local rc=1
	local COUNTER=1
	while [ $rc -ne 0 -a $COUNTER -lt $MAX_RETRY ] ; do
		sleep $DELAY
		set -x
		peer channel create -o orderer1-org1:7050 -c $CHANNEL_NAME --ordererTLSHostnameOverride orderer1-org1 -f /fabric/channel-artifacts/${CHANNEL_NAME}.tx --outputBlock $BLOCKFILE --tls --cafile $ORDERER_CA >&log.txt
		res=$?
		{ set +x; } 2>/dev/null
		let rc=$res
		COUNTER=$(expr $COUNTER + 1)
	done
	cat log.txt
	verifyResult $res "Channel creation failed"
}

# joinChannel ORG
joinChannel() {
  FABRIC_CFG_PATH=$PWD/config_blockchain/
  echo "config path is ${FABRIC_CFG_PATH}"
  ORG=$1
  PEER_NUM=$2
  infoln "Joining org: ${ORG} peer: ${PEER_NUM} to the channel..."
  setGlobals $ORG $PEER_NUM
	local rc=1
	local COUNTER=1
	## Sometimes Join takes time, hence retry
	while [ $rc -ne 0 -a $COUNTER -lt $MAX_RETRY ] ; do
    sleep $DELAY
    set -x
    peer channel join -b $BLOCKFILE >&log.txt
    res=$?
    { set +x; } 2>/dev/null
		let rc=$res
		COUNTER=$(expr $COUNTER + 1)
	done
	cat log.txt
	verifyResult $res "After $MAX_RETRY attempts, peer${PEER_NUM}.org${ORG} has failed to join channel '$CHANNEL_NAME' "
}

setAnchorPeer() {
  ORG=$1
  bash /scripts/setAnchorPeer.sh $ORG $CHANNEL_NAME
}

BLOCKFILE="/fabric/channel-artifacts/${CHANNEL_NAME}.block"


#if [[ "$CHANNEL_NAME" == "channel1" ]]
#then
  ## Create channel
infoln "Creating channel ${CHANNEL_NAME}"
createChannel 1 0
successln "Channel '$CHANNEL_NAME' created"

joinChannel 1 0
joinChannel 1 1

joinChannel 2 0
joinChannel 2 1

#fi

successln "Channel '$CHANNEL_NAME' joined"
