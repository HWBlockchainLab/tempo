#!/bin/bash


helm uninstall tempo-blockchain

kubectl delete deployment.apps/ee-channel1-peer0-org1-meter-1-0
kubectl delete deployment.apps/ee-channel1-peer1-org1-meter-1-0
kubectl delete deployment.apps/ee-channel1-peer0-org2-meter-1-0
kubectl delete deployment.apps/ee-channel1-peer1-org2-meter-1-0

kubectl delete service/ee-channel1-peer0-org1-meter-1-0
kubectl delete service/ee-channel1-peer1-org1-meter-1-0
kubectl delete service/ee-channel1-peer0-org2-meter-1-0
kubectl delete service/ee-channel1-peer1-org2-meter-1-0

kubectl delete secret ee-keys-ee-channel1-peer0-org1-meter-1-0
kubectl delete secret ee-keys-ee-channel1-peer1-org1-meter-1-0
kubectl delete secret ee-keys-ee-channel1-peer0-org2-meter-1-0
kubectl delete secret ee-keys-ee-channel1-peer1-org2-meter-1-0

kubectl delete secret influx-keys-ee-channel1-peer0-org1-meter-1-0
kubectl delete secret influx-keys-ee-channel1-peer1-org1-meter-1-0
kubectl delete secret influx-keys-ee-channel1-peer0-org2-meter-1-0
kubectl delete secret influx-keys-ee-channel1-peer1-org2-meter-1-0
