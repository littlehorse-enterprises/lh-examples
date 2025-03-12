# Cart abandonment example

## Overview

This example covers the scenario of a ecommerse shop, handling shopping cart changes, abandonment and checkout.

## Scenario Implementation

- When a user logins into the ecommerse frontend application, it initialize a shopping cart `cartId` associating a `wfRun` through cookies. See [./frontend/app/actions/login.ts](./frontend/app/actions/login.ts)
- This `cartId` is then used to mutate the `wfRun` state through `interruptHandlers` (`add-to-cart`, `checkout-completed`) making calls to `putExternalEvent` to trigger the `interruptHandlers`. See [./workflow/.../CartWorkflow.java](./workflow/src/main/java/io/littlehorse/workflow/CartWorkflow.java)


## How to use

First run

```bash
docker-compose up -d
```
It'll take a while building the docker images and a few seconds to fully deploy all the microservices:

- lh-standalone
- workflow
- notifications
- frontend

Then you can navigate to http://localhost:3000.
