
FROM hyperledger/fabric-tools:2.2.2
RUN apk update && apk add nano
ADD scripts /scripts

COPY example_packages/tempo.tgz /scripts/tmp/ccpackages/no_TLS/

