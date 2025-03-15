<p align="center">
<img alt="LittleHorse Logo" src="https://littlehorse.io/img/logo-wordmark-white.png" width="50%">
</p>

# LittleHorse .NET Quickstart

- [LittleHorse .NET Quickstart](#littlehorse-net-quickstart)
- [Prerequisites](#prerequisites)
  - [LittleHorse CLI](#littlehorse-cli)
  - [Local LittleHorse Server Setup](#local-littlehorse-server-setup)
  - [Verifying Setup](#verifying-setup)
- [Running the Example](#running-the-example)
  - [Register Metadata](#register-metadata)
  - [Run Workflow](#run-workflow)
  - [Run Task Workers](#run-task-workers)
  - [Posting an Event](#posting-an-event)
- [Next Steps](#next-steps)

**Get started in under 5 minutes, or your money back!** 😉

This repo contains a minimal example to get you started using LittleHorse in .NET. [LittleHorse](https://littlehorse.io) is a high-performance orchestration engine which lets you build workflow-driven microservice applications with ease.

You can run this example in two ways:

1. Using a local deployment of a LittleHorse Server (instructions below, requires one `docker` command).
2. Using a LittleHorse Cloud Sandbox (to get one, contact `info@littlehorse.io`).

This quickstart demonstrates a Know Your Customer (KYC) workflow. The `WfSpec` takes in three input variables (`firstName`, `lastName`, and `ssn`) which represent a customer who is attempting to sign up for some service. Before the vendor can accept the customer, they must verify the customer prospect's identity using a third-party identity verification service—for example, due to compliance reasons.

Our workflow will:

1. Execute a `TaskRun` that requests a fictitious third-party identity verification service to verify the specified customer's identity.
2. Wait for the verification process to complete (using a [LittleHorse `ExternalEvent`](https://littlehorse.io/docs/server/concepts/external-events) )
3. Either accept the customer into the system or reject them; both of which are also [LittleHorse `TaskRun`s](https://littlehorse.io/docs/server/concepts/tasks).

# Prerequisites

Your system needs:

- .NET 8 or greater
- `docker` to run the LittleHorse Server, OR access to a LittleHorse Cloud Sandbox
- Homebrew (tested on Mac or Linux) to install `lhctl`

This example uses `dotnet` to compile the .NET code.

## LittleHorse CLI

Install the LittleHorse CLI:

```sh
brew install littlehorse-enterprises/lh/lhctl
```

## Local LittleHorse Server Setup

If you have obtained a LittleHorse Cloud Sandbox, you can skip this step and just follow the configuration instructions you received from the LittleHorse Team (remember to set your environment variables!).

To run a LittleHorse Server locally in one command, you can run:

```sh
docker run --pull always --name lh-standalone --rm -d -p 2023:2023 -p 8080:8080 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
```

Using the local LittleHorse Server takes about 15-25 seconds to start up, but it does not require any further configuration. Please note that the `lh-standalone` docker image requires at least 1.5GB of memory to function properly. This is because it runs Apache Kafka, the LittleHorse Server, and the LittleHorse Dashboard all in one container.

## Verifying Setup

At this point, whether you are using a local Docker deployment or a LittleHorse Cloud Sandbox, you should be able to contact the LittleHorse Server:

```sh
>lhctl whoami
{
  "id":  {
    "id":  "anonymous"
  },
  "createdAt":  "2025-03-12T21:24:47.593Z",
  "perTenantAcls":  {},
  "globalAcls":  {
    "acls":  [
      {
        "resources":  [
          "ACL_ALL_RESOURCES"
        ],
        "allowedActions":  [
          "ALL_ACTIONS"
        ],
        "name":  ""
      }
    ]
  }
}
```

**You should also be able to see the dashboard** at `http://localhost:8080`. It should be empty, but we will put some data in there soon when we run the workflow!

If you _can't_ get the above to work, please let us know on our [Community Slack Workspace](https://launchpass.com/littlehorsecommunity). We'll be happy to help.

# Running the Example

Without further ado, let's run the example start-to-finish.

If you haven't done so already, at this point go ahead and clone this repository to your local machine.

## Register Metadata

Let's run the `Program.cs` file, which does 3 things:

1. Registers the `verify-identity`, `notify-customer-verified`, and `notify-customer-not-verified` task definitions (`TaskDef`s) with the LittleHorse Server.
2. Registers an `ExternalEventDef` named `identity-verified` with the LittleHorse Server.
3. Registers a `WfSpec` named `quickstart` with the LittleHorse Server.

A [`WfSpec`](https://littlehorse.io/docs/server/concepts/workflows) specifies a process which can be orchestrated by LittleHorse. A [`TaskDef`](https://littlehorse.io/docs/server/concepts/tasks) tells LittleHorse about a specification of a task that can be executed as a step in a `WfSpec`.

```sh
# Run the with either net8.0 or net9.0
dotnet run register -f net9.0
```

You can inspect your `WfSpec` with `lhctl` as follows. It's ok if the response doesn't make sense, we will see it soon!

```sh
lhctl get wfSpec quickstart
```

Now, go to your dashboard in your browser (`http://localhost:8080`). Click on the `quickstart` WfSpec. You should see something that looks like a flow-chart. That is your Workflow Specification!

## Run Workflow

Now, let's run our first `WfRun`! Use `lhctl` to run an instance of our `WfSpec`.

```sh
# Run the 'quickstart' WfSpec, and set 'first-name' = "John", 'last-name' = "Doe", 'ssn' = 123456789
lhctl run quickstart first-name Obi-Wan last-name Kenobi ssn 123456789
```

The response prints the initial status of the `WfRun`. Pull out the `id` and copy it!

Let's look at our `WfRun` once again. To do it with the CLI, please run:

```sh
lhctl get wfRun <wf_run_id>
```

If you would like to see it on the dashboard, go to the `WfSpec` page and scroll down. You should see your ID under the `RUNNING` column. Please double click on your `WfRun` id, and it will take you to the `WfRun` page.

Note that the status is `RUNNING`! Why hasn't it completed? That's because we haven't yet started a worker which executes the `verify-identity` and `notify-customer-verified` or `notify-customer-not-verified` tasks. Want to verify that? Let's search for all tasks in the queue which haven't been executed yet. You should see an entry whose `wfRunId` matches the Id from above:

```sh
lhctl search taskRun --taskDefName verify-identity --status TASK_SCHEDULED
```

You can also see the `TaskRun` node on the workflow. It's highlighted, meaning that it's already running! If you click on it, you can see that it is in the `TASK_SCHEDULED` status.

## Run Task Workers

Now let's start our workers, so that our blocked `WfRun` can finish. What this does is start a daemon which calls the `KnowYourCustomerTasks` C# methods for every scheduled `TaskRun` with appropriate parameters.

```sh
# Run the with either net8.0 or net9.0
dotnet run workers -f net9.0
```

Once the workers starts up, please open another terminal and inspect our `WfRun` again:

```sh
lhctl get wfRun <wf_run_id>
```

Voila! It's completed. You can also verify that the Task Queue is empty now that the Task Workers executed all of the tasks:

```sh
lhctl search taskRun --taskDefName verify-identity --status TASK_SCHEDULED
```

The example has been configured for this task to fail 25% of the time to demonstrate LittleHorse's ability to handle retries and failures.

## Posting an Event

You will notice that the `verify-identity` task completed successfully but, the workflow is still in the `RUNNING` state. This is because the workflow is waiting for an external event `identity-verified` to be posted to the workflow.

Normally in a real-world application, you would have some other service that would post an event to the workflow with webhooks. For this example, we will just use the `lhctl` command to post an event to the workflow.

```sh
lhctl postEvent <wf_run_id> identity-verified BOOL true
```

If you want the workflow to know that the identity was not able to be verified, you can post an event with a `BOOL` value of `false`.

```sh
lhctl postEvent <wf_run_id> identity-verified BOOL false
```

If you go to the `WfRun` page in your browser, you will see that the workflow has completed and the customer has been notified that whether their identity has been verified or not.

# Next Steps

If you've made it this far, then it's time you became a full-fledged LittleHorse Knight!

Visit our [docs](https://littlehorse.io/docs) or learn more about LittleHorse [here](https://littlehorse.io/learn).

Want to do more cool stuff with LittleHorse? You can find more examples [here](https://github.com/littlehorse-enterprises/littlehorse/tree/master/examples). This example only shows rudimentary features like tasks and variables. Some additional features not covered in this quickstart include:

- Loops
- Interrupts
- [User Tasks](https://littlehorse.io/docs/user-tasks-bridge)
- Multi-Threaded Workflows

We also have quickstarts in:

- [Python](../python)
- [Go](../go)
- [Java](../java)

Our extensive [documentation](https://littlehorse.io/docs) explains LittleHorse concepts in detail and shows you how take full advantage of our system.

Our LittleHorse Server is free for production use under the SSPL license. You can find our official docker image on our [GitHub Container Registry](https://github.com/littlehorse-enterprises/littlehorse/pkgs/container/littlehorse%2Flh-standalone). If you would like enterprise support, or a managed service (either in the cloud or on-prem), contact `info@littlehorse.io`.

Lastly, if you have any questions, please reach out to us on our [Community Slack Workspace](https://launchpass.com/littlehorsecommunity).

Happy riding!
