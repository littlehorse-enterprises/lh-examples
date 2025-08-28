# LittleHorse Demo's

This directory contains a few prettified LittleHorse demo applications which all demonstrate various aspects of the platform:

* [**E-Commerce**](./shopping/): an e-commerce and loyalty program application built with LittleHorse.
  * _Output Topic_ for real-time analytics to notice when a workflow has failed.
  * _Workflow Events_ to provide a synchronous wrapper over workflows.
  * [_Failure Handling_](https://littlehorse.io/docs/server/concepts/exception-handling) to handle business edge-cases.
* [**Portfolio Management**](./wealth-management/): a back-office workflow for portfolio managers to triage and handle clients in the face of a stock market event.
  * [_User Tasks_](https://littlehorse.io/docs/server/concepts/user-tasks) for human-in-the-loop approvals.
  * [_Pony ID_](https://littlehorse.io/docs/getting-started/pony-id) to easily execute User Tasks.
  * _Complex Expressions_ and arithmetic inside the workflow to determine [conditional routing](https://littlehorse.io/docs/server/concepts/conditionals).
* [**Transactional Outbox**](./transactional-outbox/): an order checkout system that demonstrates how orchestration can replace the Transactional Outbox Pattern.
  * [_Failure Handling_](https://littlehorse.io/docs/server/concepts/exception-handling)
  * [_Transactional Outbox_](https://littlehorse.io/blog/transactional-outbox)
* [**Async Bank Transfer**](./bank-transfer/): a workflow which initiates a transfer, monitors it for completion, and returns once the transfer is completed.
  * [_Loops_](https://littlehorse.io/docs/server/concepts/conditionals) are used to poll 
  * _HTTP POST Task Workers,_ which are used to make requests to existing HTTP endpoints within a task.
* [**Agentic Customer Support**](./ai-customer-support-call-actions/): models a customer support workflow in which an LLM agentically takes action by running a choice of multiple other workflows in LittleHorse.

