#!/bin/bash

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd ${SCRIPT_DIR}

VERSION='0.13.1'
ARTIFACT=lh-kafka-connect-${VERSION}
VOLUME_DIR=${SCRIPT_DIR}/connectors

# First, run the LittleHorse Kernel Standalone Image, which gives us:
# - The LH Server
# - The LH Dashboard
# - Apache Kafka
docker run --pull always --name lh-standalone --rm -d -p 2023:2023 -p 8080:8080  -p 9092:9092 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest

# Next, download and extract the LittleHorse Kafka Connectors so we can
# mount them into the Kafka Connect worker using a volume.
wget https://github.com/littlehorse-enterprises/lh-kafka-connect/releases/download/v${VERSION}/${ARTIFACT}.zip \
    -O ${SCRIPT_DIR}/${ARTIFACT}.zip
mkdir -p ${VOLUME_DIR}

unzip ${SCRIPT_DIR}/${ARTIFACT}.zip -d ${VOLUME_DIR}/${ARTIFACT}

# Next, we run a Kafka Connect image using a volume mount to add the connectors
# to the Class Path. We'll use Strimzi's image.
docker run --name kafka-connect -d --rm --net=host \
    -v ${SCRIPT_DIR}:/lh \
    apache/kafka:4.0.0 \
    /opt/kafka/bin/connect-standalone.sh \
    /lh/connect-standalone-config.properties /lh/wfrun-sink-connector-config.properties

# Lastly, we wait until Kafka is up and running and then we create the topics.
docker run --rm -it --net=host apache/kafka:4.0.0 \
    /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create \
    --topic new-customers
