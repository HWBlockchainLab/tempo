FROM centos:8

COPY proxy.crt /etc/pki/ca-trust/source/anchors/proxy.crt
RUN chmod +x /etc/pki/ca-trust/source/anchors/proxy.crt
RUN update-ca-trust


# https://stackoverflow.com/questions/70926799/centos-through-vm-no-urls-in-mirrorlist
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-Linux-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-Linux-*
RUN yum -y install /usr/bin/ps nfs-utils && yum clean all
RUN mkdir -p /exports

ADD run_nfs.sh /usr/local/bin/
RUN chmod 755 /usr/local/bin/run_nfs.sh

# expose mountd 20048/tcp and nfsd 2049/tcp and rpcbind 111/tcp
EXPOSE 2049/tcp 20048/tcp 111/tcp 111/udp

ENTRYPOINT ["/usr/local/bin/run_nfs.sh"]

# fabric setup

RUN yum -y install git
RUN yum -y install which
RUN yum -y install zip
RUN yum -y install unzip
RUN yum -y install wget

ADD build_fabric.sh /fabric_scripts/
ADD test_network_mod_files /test_network_mod_files

RUN mkdir -p /build_temp
RUN cp -r test_network_mod_files/config /build_temp/config

RUN bash /fabric_scripts/build_fabric.sh

RUN mkdir -p /exports/preprocessor_storage/config
RUN mkdir -p /exports/preprocessor_storage/jars

RUN mkdir /exports/scheduler_storage

RUN mv /build_temp/config exports/fabric/test-network/config_blockchain
RUN rm -f -r /build_temp

