# LittleHorse Examples

Our [quickstarts](./quickstart) are great for getting a LittleHorse running quickly. However, due to their simplicity they do not show everything you need to build a useful application on top of LittleHorse.

This repository is a collection of reference applications that show you how to use LittleHorse in real world scenarios / applications.

## Prerequisites

As a prerequisite to all of these examples, you need to have a LittleHorse Server running on port 2023 and `lhctl` installed.

The easiest way to get a LittleHorse Server running on port 2023 is to run the following command:

```bash
docker run --pull always --name lh-standalone --rm -d -p 2023:2023 -p 8080:8080 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
```

To install `lhctl`, you can run:

```bash
brew install littlehorse-enterprises/lh/lhctl
```
