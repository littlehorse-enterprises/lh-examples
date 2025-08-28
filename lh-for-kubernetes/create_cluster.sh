#!/bin/bash

set -ex
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

# Create a KIND cluster, which is just a way of creating a Kubernetes cluster where each
# node is just a docker container running locally.
kind create cluster --name lh-local-dev --config "${SCRIPT_DIR}/kind-config.yaml"
kubectl config use-context kind-lh-local-dev

# Basic Cluster Setup
kubectl create namespace littlehorse
kubectl config set-context --current --namespace littlehorse

# Run the Docker Image Registry as a native docker container.
#
# MacOS somehow thinks it's okay to use port 5000 for system-related stuff, so we will
# use port 5001.
docker run --rm -d -p 5001:5000 --name kind-registry registry:2

# Connect the KIND network to the Docker Image Registry.
docker network connect kind kind-registry
