#!/bin/bash

kind delete cluster --name lh-local-dev
docker kill kind-registry
