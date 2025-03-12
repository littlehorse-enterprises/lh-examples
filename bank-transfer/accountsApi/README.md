## Run littlehorse platform and user-tasks UI

### Docker run

This image is a all in one, non-production, Docker image containing the lh-server, LittleHorse user-tasks UI, and other necssary software.

```
docker run --name lh-user-tasks-standalone --rm -d\
-p 2023:2023 \
-p 8080:8080 \
-p 8888:8888 \
-p 8089:8089 \
-p 3000:3000 \
ghcr.io/littlehorse-enterprises/lh-user-tasks-api/lh-user-tasks-standalone:main
```

## Build the API

### gradle commands

`gradle build`
This will put "app.jar" into app/build/libs/app.jar

## Docker image

### build the docker image

docker build -t littlehorse/demo-accountsapi .

### run the docker image

docker run -d -p 7070:7070 littlehorse/demo-accountsapi

### TODO

- Add logging
- Better generation of demo accounts
- Docker livelyness tests
