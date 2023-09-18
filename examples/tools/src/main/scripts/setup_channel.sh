
set -e

sleep 30s # grace time for orderers to connect to each other

END=1
for i in $(seq 1 $END) ; do
bash createChannel.sh channel$i
done
