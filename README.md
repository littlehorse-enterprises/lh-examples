# LittleHorse Examples

<p align="center">
<img alt="LittleHorse Logo" src="https://littlehorse.io/img/logo.jpg" width="60%">
</p>

Welcome to LittleHorse! LittleHorse is a platform for integration, microservice orchestration, and agents all based on the open-source [LittleHorse Kernel](https://littlehorse.io/docs/server). LittleHorse allows you to:

* **Orchestrate** and execute distributed processes and workflows using the LittleHorse Kernel.
* **Capture events** and respond in real-time using [LittleHorse Connect](https://littlehorse.io/docs/lh-connect).
* **Secure** your applications and authorize your users to complete User Tasks with [Pony ID](https://littlehorse.io/docs/user-tasks-bridge).
<!-- * **Deploy** workflows (`WfSpec`s) and Task Workers with the [LittleHorse Runtime](./08-runtime/08-runtime.mdx) based on [Quarkus](https://quarkus.io). -->


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
