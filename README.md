# tempo

This is a repository for the Tempo Project, which enhances the Vanilla Hyperledger fabric with time series capabilities
and external execution capabilities for the time series capable chaincode.

# Release Notes

| Version               | Description    |
|-----------------------|----------------|
| 1.0.0 (2023.9.18) | Initial Release |

---

# Project Structure
The Tempo Projects contains the following components:
- [Chaincode External Executor](chaincode-external-executor) - The External Executor for hyperledger fabric that runs the
  Tempo smart contracts.
- [Chaincode External Interfaces](chaincode-external-interfaces) - The External Interfaces for hyperledger fabric chaincode external executor.
- [Tempo DSL](dsl) - The Tempo dsl which is used to write a Tempo oriented java project and is used by the compiler
    to generate the newly derived java projects.
- [Examples](examples) - The subproject contains examples for the Tempo project and deployment configurations for the end to end
examples.
- [TS DAL](ts-dal) - The data access layer that handle time series data persistence.
- [TSCC](tscc) - The time series interface for new APIs we now support in the user's smart contract .

---

# Usage Instructions

## Prerequisites

- Docker
- Docker-compose
- Kubernetes
- Helm
- Maven
- Java 8

## Build Project

To build the Tempo project use: "mvn clean install" in the project root.
This will build all the necessary docker images and java artifacts.

## Deploy Example

To use the given examples please refer to [these instructions](examples/README.md).