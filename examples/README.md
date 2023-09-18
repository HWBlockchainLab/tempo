# Tempo Examples

## Examples Structure
Tempo Examples contains the following folders:
- [Deployments](deployments) - Contains Helm Deployments for the end to end example.
- [NFS](nfs) - An example nfs for the Tempo end to end example which contains a unified file system for all the Tempo
components.
- [Peer](peer) - A modified version of the Hyperledger peer which includes the Tempo build pack for
  the Tempo External Executor.
- [Manual Chaincode](battery-app--chaincode-manual) - An example chaincode of a manually written chaincode that uses
the Tempo DSL without the use of the Tempo Compiler.
- [Modified Fabric Cli Container](tools) - A modified fabric cli container for the end to end example.
- [End To End Scripts](scripts) - A folder containing setup and teardown scripts for the end to end example.

## End to End Example
### Deployment Instructions
To deploy the Tempo end to end example use the [deploy scirpt](scripts/deploy_tempo.sh).

### Deployment Instructions
To teardown the Tempo end to end example use the [teardown scirpt](scripts/teardown_tempo.sh).

### End to End Example Flow
1. We deploy the example's shared file system via the NFS server.
2. We deploy an example fabric network and setup a channel.
3. We deploy a fabric chaincode example battery-app--chaincode-manual.
