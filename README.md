# LittleHorse Examples

<p align="center">
<img alt="LittleHorse Logo" src="https://littlehorse.io/img/logo.jpg" width="60%">
</p>


Welcome to LittleHorse! LittleHorse is a platform for integration, microservice orchestration, and agents all based on the open-source [LittleHorse Kernel](https://littlehorse.io/docs/server). LittleHorse allows you to:

* **Orchestrate** and execute distributed processes and workflows using the LittleHorse Kernel.
* **Capture events** and respond in real-time using [LittleHorse Connect](https://littlehorse.io/docs/lh-connect).
* **Secure** your applications and authorize your users to complete User Tasks with [Pony ID](https://littlehorse.io/docs/user-tasks-bridge).
<!-- * **Deploy** workflows (`WfSpec`s) and Task Workers with the [LittleHorse Runtime](./08-runtime/08-runtime.mdx) based on [Quarkus](https://quarkus.io). -->

- [LittleHorse Examples](#littlehorse-examples)
  - [Getting Started](#getting-started)
    - [Installing LittleHorse](#installing-littlehorse)
  - [Repository Inventory](#repository-inventory)
    - [Quickstarts](#quickstarts)
    - [Demo's](#demos)
    - [Archives](#archives)

## Getting Started

The core of our platform is the LittleHorse Kernel. Therefore, we recommend you first go through the [Kernel Quickstart](https://littlehorse.io/docs/getting-started/quickstart), located in the [`quickstart` directory](./quickstart/).

A reasonable order of operations to learn LittleHorse is:

1. Start with the [LittleHorse Kernel Quickstarts](https://littlehorse.io/docs/getting-started/quickstart), whose code lives in this repo.
2. Read the [LittleHorse Kernel Concepts](https://littlehorse.io/docs/server/concepts) documentation.
3. Go through the [LittleHorse Connect Quickstart](https://littlehorse.io/docs/getting-started/connect).
4. Start exploring! Play with one of the demo's in this repository, and look at the other platform components in [our documentation](https://littlehorse.io/docs).

### Installing LittleHorse

Install our CLI (`lhctl`) using homebrew:

```
brew install littlehorse-enterprises/lh/lhctl
```

The easiest way to get a LittleHorse Server running on port 2023 is to run the following command:

```bash
docker run --pull always --name lh-standalone --rm -d -p 2023:2023 -p 8080:8080  -p 9092:9092 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
```

The development dashboard should be available at `http://localhost:8080`.

## Repository Inventory

* Quickstarts
* Demos
* Archives

### Quickstarts

* **The Kernel**: The LittleHorse Kernel is a workflow engine for microservices and integrations and is the core of the LittleHorse Platform.
  * [**java**](./quickstart/java/): LittleHorse Kernel Java quickstart.
  * [**go**](./quickstart/go/): LittleHorse Kernel Go quickstart.
  * [**c#**](./quickstart/csharp/): LittleHorse Kernel C# quickstart.
  * [**python**](./quickstart/python/): LittleHorse Kernel Python quickstart.
  * [**scala**](./quickstart/scala/): LittleHorse Kernel Scala quickstart.
* **Ecosystem and Platform Components**
  * [**Pony ID**](./quickstart/pony-id/): an OIDC-compatible API and UI for executing [User Tasks](https://littlehorse.io/docs/server/concepts/user-tasks) in the LittleHorse Kernel.
  * [**LittleHorse Connect**](./quickstart/lh-connect/): a platform for getting data into and out of LittleHorse using Apache Kafka and Apache Kafka Connect.

### Demo's

The [demos](./demos/) directory contains a few prettified LittleHorse demo applications which all demonstrate various aspects of the platform:

* [**E-Commerce**](./demos/shopping/): an e-commerce and loyalty program application built with LittleHorse.
  * _Output Topic_ for real-time analytics to notice when a workflow has failed.
  * _Workflow Events_ to provide a synchronous wrapper over workflows.
  * [_Failure Handling_](https://littlehorse.io/docs/server/concepts/exception-handling) to handle business edge-cases.
* [**Portfolio Management**](./demos/wealth-management/): a back-office workflow for portfolio managers to triage and handle clients in the face of a stock market event.
  * [_User Tasks_](https://littlehorse.io/docs/server/concepts/user-tasks) for human-in-the-loop approvals.
  * [_Pony ID_](https://littlehorse.io/docs/getting-started/pony-id) to easily execute User Tasks.
  * _Complex Expressions_ and arithmetic inside the workflow to determine [conditional routing](https://littlehorse.io/docs/server/concepts/conditionals).
* [**Transactional Outbox**](./demos/transactional-outbox/): an order checkout system that demonstrates how orchestration can replace the Transactional Outbox Pattern.
  * [_Failure Handling_](https://littlehorse.io/docs/server/concepts/exception-handling)
  * [_Transactional Outbox_](https://littlehorse.io/blog/transactional-outbox)
* [**Async Bank Transfer**](./demos/bank-transfer/): a workflow which initiates a transfer, monitors it for completion, and returns once the transfer is completed.
  * [_Loops_](https://littlehorse.io/docs/server/concepts/conditionals) are used to poll 
  * _HTTP POST Task Workers,_ which are used to make requests to existing HTTP endpoints within a task.
* [**Agentic Customer Support**](./demos/ai-customer-support-call-actions/): models a customer support workflow in which an LLM agentically takes action by running a choice of multiple other workflows in LittleHorse.

### Archives

The [archive](./archive/) folder contains older demo's that are no longer maintained but were interesting enough to keep for posterity.
