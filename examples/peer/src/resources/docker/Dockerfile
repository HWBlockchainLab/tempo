
## Build
FROM golang:1.18 AS build


WORKDIR /app

RUN git -c http.sslVerify=false -c http.postBuffer=1048576000 clone --recursive https://github.com/hyperledger/fabric-ca.git
RUN cd /app/fabric-ca/ && CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o fabric-ca-client cmd/fabric-ca-client/main.go


## Deploy
FROM hyperledger/fabric-peer:2.2.0

RUN apk add --no-cache tzdata jq

RUN apk add --update --no-cache python3 && ln -sf python3 /usr/bin/python
RUN python3 -m ensurepip
RUN pip3 install --no-cache --upgrade pip setuptools
RUN pip3 install --no-cache --upgrade kubernetes
RUN apk add gettext

RUN rm /etc/hyperledger/fabric/core.yaml
ADD core.yaml /etc/hyperledger/fabric/core.yaml


RUN mkdir -p /external_builder
ADD build /external_builder/bin/
ADD detect /external_builder/bin/
ADD release /external_builder/bin/
ADD run /external_builder/bin/

RUN mkdir /ee_scripts
COPY ee.yaml /ee_scripts
COPY run_ee.py /ee_scripts
RUN chmod 777 -R /external_builder/bin/

COPY --from=build /app/fabric-ca/fabric-ca-client /usr/local/bin/

