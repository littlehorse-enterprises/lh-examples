# Quarkus Docker Example

This project (sub-folder) contains an implementation of a few Task Workers + a `WfSpec` using the `littlehorse-quarkus` extension and Quarkus.

## Running Locally

```
./gradlew quarkusDev
```

## Building the Docker Image

```
quarkus build image
```

Note that the result of building this image is pushed to `ghcr.io/littlehorse-enterprises/lh-examples/example-worker-pod`.

## Running the Image

You can run the image as follows:

```
docker run --rm ghcr.io/littlehorse-enterprises/lh-examples/worker-pod
```

You can configure the client with environment variables, as follows:

```
docker run --rm \
    -e LHC_API_HOST=test.some.domain \
    -e LHC_API_PORT=2024 \
    ghcr.io/littlehorse-enterprises/lh-examples/worker-pod
```
