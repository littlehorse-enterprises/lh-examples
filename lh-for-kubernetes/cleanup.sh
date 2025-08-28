#!/bin/bash

kind delete cluster --name lh-local-dev
docker kill kind-registry
docker network rm kind
