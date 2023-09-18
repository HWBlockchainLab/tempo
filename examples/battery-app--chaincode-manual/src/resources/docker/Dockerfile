FROM ubuntu:18.04

# Install OpenJDK-11
RUN apt-get clean && \
    apt-get update --fix-missing  && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y ant;

# Fix certificate issues
#RUN apt-get install ca-certificates-java && \
#    apt-get clean && \
#    update-ca-certificates -f;
RUN apt-get install ca-certificates-java && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME
ENV PATH ${PATH}:${JAVA_HOME}/bin

COPY entrypoint.sh entrypoint.sh
RUN chmod 755 ./entrypoint.sh

COPY ./chaincode.jar app.jar

RUN mkdir -p /tmp/wallet
RUN mkdir -p /tmp/checkpoint
RUN mkdir -p /tmp/config
COPY logging.properties /tmp/config/

EXPOSE 20002

CMD ["./entrypoint.sh"]
