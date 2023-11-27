# LittleHorse Examples

Our quickstarts (for example, [Java](https://github.com/littlehorse-enterprises/lh-quickstart-java) and [Go](https://github.com/littlehorse-enterprises/lh-quickstart-go)) are great for getting a LittleHorse Workflow running quickly. However, due to their brevity they do not show everything you need to build a useful application on top of LittleHorse.

This repository will grow into a collection of reference applications that show you how to use LittleHorse in various different situations.

## Prerequisites

As a prerequisite to all of these examples, you should have access to a LH Server and `lhctl`. To install the proper version of `lhctl`, you can run:

```
go install github.com/littlehorse-enterprises/littlehorse/lhctl@0.7.0-alpha.1
```

To run a LittleHorse Cluster (Kafka, LH Server, and soon to be LH Dashboard) in a single container on your laptop, you can run:

```
docker run --name littlehorse -d -p 2023:2023 public.ecr.aws/littlehorse/lh-standalone:latest
```

The `lh-standalone` image takes about 30 seconds to start up because we first need to start a Kafka cluster. Once LH is up and running, you should get a `[]` response from:
```
lhctl search wfSpec
```

_NOTE: if your computer falls asleep for a while, you might benefit from stopping, removing, and re-starting the lh-standalone container._

## Application and Repo Structure

An application generally has the following components:
* Program that generates and registers a `WfSpec`
* A few Task Workers
* Backend-for-Frontend that translates LittleHorse Primitives into Business Domain Objects.
* A LittleHorse Cluster. In this case, the easiest way to get that is to use the `lh-standalone` image.

In LittleHorse, one `TaskDef` can be used in different `WfSpec`s; which means that one Task Worker can be used in multiple applications.

The following is the repo structure:

### `common-tasks`

The [`common-tasks`](./common-tasks) folder contains a set of Task Worker implementations that can be independently used by various different applications.

Each task, regardless of what language it is written in, has the following two bash scripts:
1. `.../register.sh`, which registers the `TaskDef` to the LittleHorse Server.
2. `.../run-worker.sh`, which starts the Task Worker so that it can execute tasks.

Note that if a `WfSpec` for an application depends on a `TaskDef`, you must run the `.../register.sh` for the relevant `TaskDef`s before attempting to deploy the `WfSpec`; else, the `rpc PutWfSpec` will fail.

### `applications`

The [`applications`](./applications) folder contains a set of various demo applications that use LittleHorse. Each application will have some or all of the following components:

* A program that registers a `WfSpec` (every application has this)
* A backend-for-frontend or REST API
* A frontend which drives the workflow

Please consult the README of each individual application for instructions about how to run that app.
