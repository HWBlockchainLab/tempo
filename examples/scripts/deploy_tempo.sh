#!/bin/bash

set -e

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR/../deployments

## setup network and third parties
helm install tempo-nfs-server ./tempo-nfs-server
helm install tempo-blockchain ./tempo-fabric-blockchain
sleep 10s

## setup channel
kubectl exec -ti $(kubectl get pods |grep fabric-cli|awk '{print $1}') -- bash /scripts/setup_channel.sh

meterPackageName="deploy-package-meter.tgz"

meterPackagePath="$SCRIPT_DIR/$meterPackageName"

kubectl cp $meterPackagePath $(kubectl get pods |grep fabric-cli|awk '{print $1}'):/scripts/tmp/ccpackages/no_TLS/

kubectl exec -ti $(kubectl get pods |grep fabric-cli|awk '{print $1}') -- bash deployCC.sh channel1 meter /scripts/tmp/ccpackages/no_TLS/$meterPackageName java
