# LittleHorse for Kubernetes

In this quickstart, we will:

* Use the LittleHorse Operator to deploy an Apache Kafka® cluster using the `LHKafka` CRD.
* Use the `LHCluster` CRD to create a minimal LittleHorse cluster.

**As a prerequisite, make sure you did the "Common Setup" in the [previous README](../README.md#common-setup).** This will:

* Configure `/etc/hosts` properly.
* Create a KIND cluster.
* Install the LittleHorse Kubernetes Operator.

## Start Apache Kafka®

To start an Apache Kafka cluster using LHK, you can apply the `01-kafka.yaml` file.

```
kubectl apply -f 01-kafka.yaml
```

After 60 seconds or so, you should see the following in your KIND cluster:

```
-> kubectl get pods --namespace littlehorse
NAME                                        READY   STATUS    RESTARTS   AGE
lh-kafka-cruise-control-7477c964d4-zphh9    1/1     Running   0          32s
lh-kafka-entity-operator-97bc49544-jl2xj    2/2     Running   0          54s
lh-kafka-lh-kafka-controller-0              1/1     Running   0          114s
lh-kafka-lh-kafka-controller-1              1/1     Running   0          114s
lh-kafka-lh-kafka-controller-2              1/1     Running   0          114s
lh-lh-operator-6b5769ddbc-htgt4             1/1     Running   0          3m13s
strimzi-cluster-operator-6f6d7f4bc7-w6c6h   1/1     Running   0          3m13s

-> kubectl get lhkafka --namespace littlehorse
NAME       KAFKAVERSION   PROBLEMS
lh-kafka   3.8.0
```

## Start LittleHorse

Next, start LittleHorse:

```
kubectl apply -f 02-littlehorse.yaml
```

When the pods come up, it should look as follows:

```
-> kubectl get lhcluster --namespace littlehorse
NAME         INSTANCES   OFFLINE_TASKS   UNDER_REPLICATED_TASKS   WARMUP_TASKS   PROBLEMS
quickstart   1           0               0                        0

-> kubectl get pods --namespace littlehorse
NAME                                        READY   STATUS    RESTARTS   AGE
lh-kafka-cruise-control-7477c964d4-zphh9    1/1     Running   0          74m
lh-kafka-entity-operator-97bc49544-jl2xj    2/2     Running   0          74m
lh-kafka-lh-kafka-controller-0              1/1     Running   0          75m
lh-kafka-lh-kafka-controller-1              1/1     Running   0          75m
lh-kafka-lh-kafka-controller-2              1/1     Running   0          75m
lh-lh-operator-6b5769ddbc-htgt4             1/1     Running   0          77m
quickstart-dashboard-855bfc75db-lrwzr       1/1     Running   0          79s
quickstart-server-0                         1/1     Running   0          78s
strimzi-cluster-operator-6f6d7f4bc7-w6c6h   1/1     Running   0          77m
```

## Run a Workflow

In this section, we'll create a `Deployment` that registers a `WfSpec` and runs Task Workers from a few pods inside the KIND cluster. Then we'll port-forward to the LH Server in order to access it with `lhctl`.

### Deploy the WfSpec and Task Workers

The [Quarkus Docker](../../docker/quarkus/) example builds a docker image that registers a `WfSpec` and starts up a few Task Workers. We will deploy that container as a `Deployment` and configure it to talk to the LittleHorse Cluster that we just deployed.

```
kubectl apply -f 03-worker-pod.yaml
```

### Run the Workflow with `lhctl`

Let's first port-forward to the LittleHorse Server. This will always work for `lhctl`.

```
kubectl port-forward quickstart-server-0 2023:2023
```

In a different terminal window, you should be able to view the `WfSpec` via `lhctl search wfSpec`:

```
-> lhctl search wfSpec
{
  "results":  [
    {
      "name":  "customer-arrival",
      "majorVersion":  0,
      "revision":  0
    }
  ]
}
```

Finally, let's run the workflow a few times!

```
lhctl run customer-arrival customer-id Obi-Wan
lhctl run customer-arrival customer-id Anakin
```

**_NOTE_**: subsequent examples (coming soon) will show how to access the LittleHorse Kernel from outside the Kubernetes cluster using the Gateway API or the Ingress API in a production-ready manner.

### View the Dashbaord

You can access the dashboard as follows:

```
kubectl port-forward svc/quickstart-dashboard 3000:3000
```

Then open it in `localhost:3000`!

If you ran the above commands and explore the resulting `WfRun`'s, you can see that the author of the example worker and workflow recognizes that Obi-Wan is indeed a Jedi Master, but Anakin is not.

## Client Access From Outside the Cluster

While `lhctl` doesn't need the ability to access LittleHorse Servers individually, Task Workers need to be able to specifically address each individual server. Therefore, setting up networking for Task Worker access from outside the K8s cluster is a bit more involved (we have subsequent examples of how to do that!).

However, if you look closely enough at `02-littlehorse.yaml` we did a clever hack that allows the port-forwarding to work with a task worker configured to talk to `localhost:2023`:

1. We only have one server, which is accessible on `localhost:2023` due to port-forwarding.
2. That server advertises `localhost:2023` as its advertised host to the client.

Therefore, you should be able to write normal python/java/go/c# task workers and run them on your local terminal and it will work just like when you run the `lh-standalone` image locally.
