#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#

# This is a collection of bash functions used by different scripts

# imports
. /scripts/utils.sh

export CORE_PEER_TLS_ENABLED=true
export ORDERER_CA=${PWD}/organizations/ordererOrganizations/org1/orderers/orderer1.org1/msp/tlscacerts/tlsca.org1-cert.pem

# Set environment variables for the peer org
setGlobals() {
  PEER_NUM=$2
  local USING_ORG=""
  if [ -z "$OVERRIDE_ORG" ]; then
    USING_ORG=$1
  else
    USING_ORG="${OVERRIDE_ORG}"
  fi
  infoln "Using organization ${USING_ORG}"
  if [ $USING_ORG -eq 1 ]; then
    export CORE_PEER_LOCALMSPID="Org1MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer${PEER_NUM}.org1.example.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
    if [ $PEER_NUM -eq 0 ]; then
      export CORE_PEER_ADDRESS=peer0-org1:7051
    elif [ $PEER_NUM -eq 1 ]; then
      export CORE_PEER_ADDRESS=peer1-org1:8051
    elif [ $PEER_NUM -eq 2 ]; then
      export CORE_PEER_ADDRESS=peer2-org1:8751
    elif [ $PEER_NUM -eq 3 ]; then
      export CORE_PEER_ADDRESS=peer3-org1:8761
    fi
        
  elif [ $USING_ORG -eq 2 ]; then
    export CORE_PEER_LOCALMSPID="Org2MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer${PEER_NUM}.org2.example.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
    if [ $PEER_NUM -eq 0 ]; then
      export CORE_PEER_ADDRESS=peer0-org2:9051
    elif [ $PEER_NUM -eq 1 ]; then
      export CORE_PEER_ADDRESS=peer1-org2:10051
    elif [ $PEER_NUM -eq 2 ]; then
      export CORE_PEER_ADDRESS=peer2-org2:10651
    elif [ $PEER_NUM -eq 3 ]; then
      export CORE_PEER_ADDRESS=peer3-org2:10751
    fi

  elif [ $USING_ORG -eq 3 ]; then
    export CORE_PEER_LOCALMSPID="Org3MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org3.example.com/peers/peer${PEER_NUM}.org3.example.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org3.example.com/users/Admin@org3.example.com/msp
    if [ $PEER_NUM -eq 0 ]; then
      export CORE_PEER_ADDRESS=peer0-org3:12700
    elif [ $PEER_NUM -eq 1 ]; then
      export CORE_PEER_ADDRESS=peer1-org3:12710
    elif [ $PEER_NUM -eq 2 ]; then
      export CORE_PEER_ADDRESS=peer2-org3:12720
    elif [ $PEER_NUM -eq 3 ]; then
      export CORE_PEER_ADDRESS=peer3-org3:12730
    fi
  else
    errorln "ORG Unknown"
  fi

  if [ "$VERBOSE" == "true" ]; then
    env | grep CORE
  fi
}

# Set environment variables for orderer
setGlobalsOrderer() {
  export CORE_PEER_LOCALMSPID="OrdererMSP"
  export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/ca.crt
  export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/ordererOrganizations/example.com/users/Admin@example.com/msp/
  export CORE_PEER_ADDRESS=localhost:7051
  unset https_proxy
  unset http_proxy
	
  env | grep CORE
}

# Set environment variables for use in the CLI container 
setGlobalsCLI() {
  setGlobals $1

  local USING_ORG=""
  if [ -z "$OVERRIDE_ORG" ]; then
    USING_ORG=$1
  else
    USING_ORG="${OVERRIDE_ORG}"
  fi
  if [ $USING_ORG -eq 1 ]; then
    export CORE_PEER_ADDRESS=peer0-org1:7051
  elif [ $USING_ORG -eq 2 ]; then
    export CORE_PEER_ADDRESS=peer0-org2:9051
  elif [ $USING_ORG -eq 3 ]; then
    export CORE_PEER_ADDRESS=peer0-org3:12700
  else
    errorln "ORG Unknown"
  fi
}

# parsePeerConnectionParameters $@
# Helper function that sets the peer connection parameters for a chaincode
# operation
parsePeerConnectionParameters() {
  PEER_CONN_PARMS=""
  PEERS=""
  while [ "$#" -gt 0 ]; do
    setGlobals $1 0
    PEER="peer0.org$1"
    ## Set peer addresses
    PEERS="$PEERS $PEER"
    PEER_CONN_PARMS="$PEER_CONN_PARMS --peerAddresses $CORE_PEER_ADDRESS"
    ## Set path to TLS certificate
    TLSINFO=$(eval echo "--tlsRootCertFiles $CORE_PEER_TLS_ROOTCERT_FILE")
    PEER_CONN_PARMS="$PEER_CONN_PARMS $TLSINFO"
    # shift by one to get to the next organization
    shift
  done
  # remove leading space for output
  PEERS="$(echo -e "$PEERS" | sed -e 's/^[[:space:]]*//')"
}

verifyResult() {
  if [ $1 -ne 0 ]; then
    fatalln "$2"
  fi
}
