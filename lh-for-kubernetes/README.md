# LittleHorse for Kubernetes

This directory contains examples for the LittleHorse for Kubernetes project. 

**You will need to contact LittleHorse Enterprises (`info@littlehorse.io`) in order to access credentials to download our Operator docker image.**

These examples utilize [KIND](https://kind.sigs.k8s.io/) (Kubernetes-in-Docker) clusters to run. System requirements are:

* [Docker](https://www.docker.com)
* [KIND](https://kubernetes.io/docs/tasks/tools/#kind)
* [`lhctl`](https://littlehorse.io/docs/developer-guide/install)
* [`helm`](https://helm.sh)
* [`kubectl`](https://kubernetes.io/docs/tasks/tools/#kubectl)

## What is LittleHorse for Kubernetes?

LittleHorse for Kubernetes is a management suite (delivered via Kubernetes Operator) that makes it easy to run production-grade installations of the LittleHorse Platform on Kubernetes. It allows you to:

* Run production-ready [LittleHorse Kernel](https://littlehorse.io/docs/server) clusters.
  * Automatically encrypt communication between LH Server instances.
  * Configure persistent storage.
  * Encrypt and authenticate communication between LittleHorse and Kafka.
  * Configure `Ingress` or `TLSRoute` resources to allow north-south traffic to LittleHorse.
  * Manage LittleHorse `Tenant`s and `Principal`s.
  * Handle authentication through OIDC or MTLS.
* Run Apache Kafka (dependency of the LittleHorse Cluster) with ease.
  * Automate the loading of plugins into `KafkaConnect` clusters at Strimzi
    * The `LHKafka` CRD wraps `Kafka`, `KafkaNodePool`, `KafkaRebalance`, and `KafkaConnect`.
    * The `LHKafkaConnector` CRD automatically updates the plugins in the Kafka Connect cluster for the `LHKafka`.
* Run and administer Keycloak securely.
* _COMING SOON:_ Run [Pony ID](https://littlehorse.io/docs/getting-started/pony-id) on Kubernetes.
  * Deploy the User Tasks Backend.
  * Deploy the User Tasks Console.
  * Configure Brokering to your own OIDC Provider.

## The Examples

You can find the following examples in this directory:

* [**Quickstart**](./quickstart/): get the LittleHorse Kernel running on Kubernetes and connect to it from a `Pod` inside the cluster.

<!-- The below coming soon: -->
<!-- * [**Gateway API**](./gateway-api/): connect to the LittleHorse Kernel using the Gateway API (`TLSRoute`) in a KIND cluster.
* [**Ingress**](./gateway-api/): connect to the LittleHorse Kernel using the `Ingress` in a KIND cluster. -->

## Common Setup

All of the examples in this directory will utilize the same system setup: a KIND cluster. Please complete all of the following steps before moving to one of the guided tours. 

### Configure `/etc/hosts`

Accessing the LittleHorse Kernel from outside a Kubernetes Cluster is a similar challenge to accessing Apache Kafka from outside a Kubernetes Cluster. To understand the nuts and bolts of it, we recommend you read this [blog by our Founder](https://strimzi.io/blog/2024/08/16/accessing-kafka-with-gateway-api/) about accessing Strimzi-managed Kafka from outside a KIND cluster.

Put the following entries into your `/etc/hosts` file (source: trust me bro).

```
# LittleHorse for Kubernetes Examples
127.0.0.1 lh.lhk.littlehorse.local
127.0.0.1 lh-0.lhk.littlehorse.local
127.0.0.1 lh-1.lhk.littlehorse.local
127.0.0.1 lh-2.lhk.littlehorse.local
127.0.0.1 dashhboard.lhk.littlehorse.local
```

### Create the KIND Cluster

We will now:

* Create a KIND cluster.
* Configure mappings from certain ports on your local machine into specific ports on the Kubernetes Node of the KIND cluster. Note that the following ports must be 
  * `80`
  * `443`
  * `2023`
  * `2024`
  * `9092`
* Deploy a local image registry which can be used for the Kaniko Build feature of certain 

Before running this script, please ensure that you do not have any other `KIND` clusters running, and also that you do not have any processes utilizing the ports mentioned above!

```
./create_cluster.sh
```

### Install the Operator

Before you can install the LittleHorse Kubernetes Operator, you must create a `Secret` containing the Quay credentials that the LittleHorse Enterprises team provided for you.

```
kubectl apply -f image-pull-secret.yaml # This file comes from the LittleHorse Enterprises team (info@littlehorse.io)
```

Next, add the helm repo with the LittleHorse Operator's Helm Chart:

```
helm repo add littlehorse https://littlehorse-enterprises.github.io/lh-helm-charts/
```

Finally, you can install the LittleHorse Operator via the following commands:

```
helm repo update
helm upgrade --install lh \
    --version 0.13.0 \
    --namespace littlehorse \
    --set image.repository=quay.io/littlehorse/lh-operator \
    --set image.tag=0.13.0 \
    --set strimzi.deploy=true \
    --set strimzi.enabled=true \
    --set 'imagePullSecrets[0].name=littlehorse-pull-secret' \
    littlehorse/lh-operator
```

Note that this command also installs [Strimzi](https://strimzi.io), which is a dependency of the `LHKafka` CRD (our opinionated and convenient wrapper around Strimzi's `Kafka`, `KafkaNodePool`, `KafkaUser`, and `KafkaConnect` CRD's, tailored for the LittleHorse use-cases).

### Checkpoint

You should see the following pods in your Kubernetes cluster:

```
-> kubectl get pods --all-namespaces
NAMESPACE            NAME                                                 READY   STATUS    RESTARTS   AGE
kube-system          coredns-668d6bf9bc-bqvqk                             1/1     Running   0          85s
kube-system          coredns-668d6bf9bc-lbzvh                             1/1     Running   0          85s
kube-system          etcd-lh-local-dev-control-plane                      1/1     Running   0          92s
kube-system          kindnet-d7hnv                                        1/1     Running   0          85s
kube-system          kube-apiserver-lh-local-dev-control-plane            1/1     Running   0          92s
kube-system          kube-controller-manager-lh-local-dev-control-plane   1/1     Running   0          92s
kube-system          kube-proxy-s4jtd                                     1/1     Running   0          85s
kube-system          kube-scheduler-lh-local-dev-control-plane            1/1     Running   0          92s
littlehorse          lh-lh-operator-6b5769ddbc-g278n                      1/1     Running   0          54s
littlehorse          strimzi-cluster-operator-6f6d7f4bc7-smz7b            1/1     Running   0          54s
local-path-storage   local-path-provisioner-58cc7856b6-fn5rm              1/1     Running   0          85s
```

Now, you're ready to get going with the examples!

### Cleanup and Troubleshooting

You can cleanup the KIND cluster by running:

```
./cleanup.sh
```

There is sometimes a bug in KIND in which the docker network for the KIND cluster is not deleted properly. If you run the cleanup script and still see `kind` under the output of `docker network ls`, you should remove it via:

```
docker network rm kind
```

If you do not do so, you get some VERY weird networking issues inside your KIND cluster.
