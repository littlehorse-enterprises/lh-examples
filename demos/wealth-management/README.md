<p align="center">
<img alt="LittleHorse Logo" src="https://littlehorse.io/img/logo-wordmark-white.png" width="50%">
</p>

# Wealth Advisor Workflow

This repository demonstrates a skeleton portfolio management workflow, in which we respond to a market event that causes a significant loss of value in many portfolios.

The workflow is intended to be run for every portfolio with exposure to the affected industry. In this workflow, we:

1. Determine if the overall value of the portfolio decreased by more than 5%. If not, then we take no action.
2. Determine whether the portfolio is a "high-value" portfolio. If not, we simply generate a report and send it to the portfolio owner.
3. For a high-value account, we ask the Wealth Advisor team to reach out to the affected investors, and wait for a meeting to be confirmed. Once the meeting is confirmed we store the information in our CRM.

## Relevant LittleHorse Concepts

This workflow uses the following LittleHorse concepts:

1. [Tasks](https://littlehorse.io/docs/server/concepts/tasks)
2. [User Tasks](https://littlehorse.io/docs/server/concepts/user-tasks), in which we ask a human user to perform some task.
3. [External Events](https://littlehorse.io/docs/server/concepts/external-events), in which we wait for something to happen from outside the `WfRun`.
4. [Conditional logic](https://littlehorse.io/docs/server/)

This example also uses the [User Tasks Bridge](https://littlehorse.io/docs/user-tasks-bridge) product, which connects your identity provider to User Tasks in LittleHorse.

## Running the Example

You need the following dependencies:
* `gradle`
* `java`
* `docker`
* `lhctl` (you can get this with `brew install lhctl`).

### Setup

First, start the User Tasks Bridge local development docker image:

```sh
docker run --pull always --name lh-user-tasks-bridge-standalone --rm -d --net=host \
    ghcr.io/littlehorse-enterprises/lh-user-tasks-bridge-backend/lh-user-tasks-bridge-standalone:latest
```
The standalone container starts the following components:

* LittleHorse Server (gRPC 2023)
* LittleHorse Dashboard (http://localhost:8080)
* Keycloak Identity Provider (http://localhost:8888)
* User Tasks Bridge API (http://localhost:8089)
* User Tasks Bridge UI (http://localhost:3000)

We will use Keycloak as our identity provider to log into the User Tasks Bridge UI, which is where we will execute the User Task in our workflow.

### Deploying the `WfSpec`

In LittleHorse, you [first need to register the `WfSpec`](https://littlehorse.io/docs/server/concepts) before you can run it. You must also start some task workers to execute the tasks.

In this example, we have a Java application that does all of the following:

1. Register the required metadata such as `WfSpec`s and `TaskDef`s and `UserTaskDef`s for our workflow.
2. Start a bunch of Task Workers that will execute tasks such as fetch information about a portfolio, generate reports, and update a fictitious CRM with information about recent contacts with customers.
3. Expose a REST server that allows us to trigger our `WfSpec` and post an `ExternalEvent` that represents a meeting being confirmed.

You can do all of that with one simple command:

```
./gradlew run
```

### Running the Workflow

When running the `WfSpec`, we must pass in the `portfolio-id` variable. You can run the `WfRun` from the dashboard, or:

With `lhctl` (note the `--wfRunid` argument is optional, but if you do not provide it, make a note of the id that is returned by the command.)

```
lhctl run portfolio-notification portfolio-id 123 --wfRunId my-wf-run
```

With our REST API:

```
curl -s localhost:5000/initiate-response -d '{
  "portfolioId": "123"
}'
```

Make sure to make a note of the ID that is returned from the curl command.

### Executing the User Task

To execute the UserTaskRun, log in to the User Tasks Bridge UI using Keycloak. The `UserTaskRun` will be assigned to the user `dave-ramsey`, so if you wait 60 seconds it will be re-assigned to the `wealth-managers` group.

### Posting the ExternalEvent

You must post an external event to simulate the meeting being confirmed. You can do it as follows. Remember to change `another-wf-run` to whatever the `WfRunId` is from the previous step.

```
curl -d '{"date": 1738571497133, "zoomLink": "https://somecompany.zoom.us/some-meeting-id"}' http://localhost:5000/confirm-meeting/another-wf-run
```
