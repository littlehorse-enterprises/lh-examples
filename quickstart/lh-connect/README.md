# LittleHorse Connect Quickstart

In this quickstart, we willl

> NOTE: This quickstart depends on the `--net=host` option in `docker run`, which may not work for MacOS users.

## Setup

The setup for this quickstart involves two steps:

1. Set up your environment with Kafka, the LittleHorse Kernel, and Kafka Connect. This step also configures the LittleHorse `WfRunSinkConnector` inside the Kafka Connect cluster.
2. Register the `quickstart` workflow and run the workers.

### Deploy Containers

> _NOTE: You may need to delete the `lh-standalone` container from previous runs. You can do that with `docker kill littlehorse && docker rm littlehorse`._

This step will deploy:

1. `lh-standalone`, which includes the LittleHorse Kernel (both server and dashboard) and an Apache Kafka cluster.
2. A Kafka Connect worker running in standalone mode.

```sh
./setup.sh
```

### Run the Normal Quickstart

Next, register the `quickstart` `WfSpec` in a language of your choice. For example, in Java:

```sh
cd ../java
./gradlew run --args register
./gradlew run --args workers
```

If you go to the dashboard (`http://localhost:8080`) you should see the `WfSpec`.

## Produce Records to Kafka

Lastly, all we need to do to run the `WfSpec` is produce records to Kafka. As you'll recall, the `quickstart` `WfSpec` accepts three input variables:

* `first-name`: a `STR`
* `last-name`: a `STR`
* `ssn`: an `INT`

The `WfRunSinkConnector` is configured (via `wfrun-sink-connector-config.properties`) to accept JSON's with each field being a variable passed into the `WfRun`. 

Start up a Kafka Console Producer as follows:

```sh
docker run -it --rm --net=host apache/kafka:4.0.0 \
  /opt/kafka/bin/kafka-console-producer.sh \
  --topic new-customers \
  --bootstrap-server localhost:9092 \
```

And here are some records to produce:

```
{"first-name":"Obi-Wan", "last-name": "Kenobi", "ssn": 12345}
{"first-name":"Anakin", "last-name": "Skywalker", "ssn": 54321}
```

The `WfRun`s should show up in the dashboard now!

## Cleanup

You can clean everything up via:

```sh
./cleanup.sh
```
