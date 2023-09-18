
set -e
workDir=/fabricSamplesDir
repo_path=/test_network_mod_files
fabric_path=/fabric-samples/test-network

echo -e "\n============ downloading fabric-samples ============\n"
cd /
mkdir -p ${workDir}
cd ${workDir}

curl --insecure -sSL https://bit.ly/2ysbOFE |
sed 's/curl/curl --insecure/g' |
sed 's/git clone/git -c http.sslVerify=false clone/g' |
sed 's/DOCKER=true/DOCKER=false/g' |
bash -s -- 2.2.2 1.4.9

echo -e "\n============ running copy_to_net============\n"

cp ${repo_path}/crypto_config/crypto-config-orderer.yaml ${workDir}/${fabric_path}/organizations/cryptogen/crypto-config-orderer.yaml
cp ${repo_path}/crypto_config/crypto-config-org1.yaml ${workDir}/${fabric_path}/organizations/cryptogen/crypto-config-org1.yaml
cp ${repo_path}/crypto_config/crypto-config-org2.yaml ${workDir}/${fabric_path}/organizations/cryptogen/crypto-config-org2.yaml

cp ${repo_path}/create_crypto_org.sh ${workDir}/${fabric_path}/create_crypto_org.sh
cp ${repo_path}/create_crypto_channel.sh ${workDir}/${fabric_path}/create_crypto_channel.sh

echo -e "\n== generating crypto data and channel information ==\n"
cd ${workDir}${fabric_path}

bash ./create_crypto_org.sh

END=10
for i in $(seq 1 $END) ; do
bash ./create_crypto_channel.sh channel$i Channel$i
done


cp ${repo_path}/crypto_config/client-network-connection-org1.json ${workDir}/${fabric_path}/organizations/peerOrganizations/org1.example.com/client-network-connection-org1.json

mkdir -p /exports/fabric
cp -r ${workDir}${fabric_path} /exports/fabric/test-network
