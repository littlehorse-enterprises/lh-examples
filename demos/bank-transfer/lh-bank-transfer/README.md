# Bank Transfer Workflow

## Information

In this workflow we will simulate a simple bank transfer.  

The steps of the workflow are as follows:

1) validate that the data sent contains the necessary information to complete a transfer.
2) Check for a valid From account.
3) Check for a valid To account.
4) If the transfer is greater than $50,000 wait for a human to approve
5) after the transfer is initiated every 10second check to verify that the transfer has completed successfully or has failed.

## Setup

1. We need to have a running LittleHorse instance. We will be using our [User Tasks Standalone](https://github.com/littlehorse-enterprises/lh-user-tasks-api/blob/main/standalone/README.md) image for this to also run the User Tasks UI.
```docker run --name lh-user-tasks-standalone --rm -d -p 2023:2023 -p 8080:8080 -p 8888:8888 -p 8089:8089 -p 3000:3000 ghcr.io/littlehorse-enterprises/lh-user-tasks-api/lh-user-tasks-standalone:main```

2. run `gradle build` to build the jars
3. run `./gradlew run --args register` to register the `TaskDefs` and `WfSpec` in LittleHorse

### Starting Task Workers

#### Running with Docker

##### Build the docker image

```docker build -t littlehorse/demo-bank-transfer .```

##### run the docker image

```docker run --network host -d littlehorse/demo-bank-transfer```

#### Running without Docker

Run the following commands in different terminals to start the task workers:

1. fetch account details

```bash
java -jar app/build/libs/app.jar fetch-account
```

2. initiate transfer

```bash
java -jar app/build/libs/app.jar initiate-transfer
```

3. check transfer status

```bash
java -jar app/build/libs/app.jar check-transfer
```

## Run Workflow

```bash
lhctl run initiate-transfer transferDetails '{
        "fromAccountId": "1234",
        "toAccountId": "4564",
        "amount": 500.23,
        "currency": "USD",
        "description": "1234 to 4564"
}'
```
