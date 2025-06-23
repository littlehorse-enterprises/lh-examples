#!/bin/bash

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd ${SCRIPT_DIR}

rm -r connectors
rm -r lh-kafka-connect-0.13.1.zip

docker kill lh-standalone ; docker rm lh-standalone
docker kill kafka-connect ; docker rm kafka-connect
