# IT Request

- [IT Request](#it-request)
  - [Overview](#overview)
    - [Business Process](#business-process)
    - [Relevant Features](#relevant-features)
  - [For Developers](#for-developers)
    - [Required Systems](#required-systems)
    - [Set Up the App](#set-up-the-app)
      - [Register TaskDef](#register-taskdef)
      - [Register the Workflow Spec](#register-the-workflow-spec)
  - [Run the Workflow with `lhctl`](#run-the-workflow-with-lhctl)
    - [Run the `WfRun`](#run-the-wfrun)
    - [Test Search Capabilities](#test-search-capabilities)
    - [Assign the User Task Run](#assign-the-user-task-run)
    - [Check Search Again](#check-search-again)
    - [Execute the User Task](#execute-the-user-task)
    - [Search One Last Time](#search-one-last-time)
  - [Run the Workflow With REST API](#run-the-workflow-with-rest-api)
  - [Future Work and Extensions](#future-work-and-extensions)

## Overview

This directory contains a rudimentary and simplified version of an IT Request Ticketing system built on top of LittleHorse. The purpose of this system is to demonstrate how to use several LittleHorse capabilities in a practical manner; however, the application itself is not a complete and production-ready application.

### Business Process

In this simplified workflow, an IT request is initiated when someone submits a request. The request contains the email address of the person submitting a request, the name of the item they want IT to buy for them, and a justification of why they need that item. Through the [User Tasks](https://littlehorse.dev/docs/concepts/user-tasks) feature, a person from the Finance department of our make-believe company either approves or rejects the request. The original requester is notified of the results via email.

### Relevant Features

The following LittleHorse features are used by the `WfSpec` (Workflow Specification) itself:
* [Workflow Variables](https://littlehorse.dev/docs/concepts/variables)
* Basic [User Tasks](https://littlehorse.dev/docs/concepts/user-tasks) (tasks assigned to humans)
* [Conditional Branching](https://littlehorse.dev/docs/concepts/conditionals)
* [Tasks](https://littlehorse.dev/docs/concepts/tasks) (assigned to computers)

This application also includes a Spring Boot API which presents the `it-request` Domain Object in a RESTful manner. This API uses the following concepts:
* Integration with Spring Boot
* Searching for `Variable`s by their values with the `SearchVariable` rpc
* Searching for User Tasks via the `SearchUserTaskRun`
* Assigning a User Task via the `AssignUserTaskRun` rpc
* Executing a User Task via the `CompleteUserTaskRun` rpc
* Running a workflow (creating a `WfRun`)
* Using LittleHorse `WfSpec`s to model domain information

## For Developers

There are two sub-directories in this application:
1. `workflow`, which contains the program that generates and registers the `WfSpec`.
2. `rest-api`, which is a Spring Boot application that exposes a clean RESTful interface for the system.

Note that in this application, all data is stored as LittleHorse `WfRun`s, `Variable`s, and `UserTaskRun`s: there is no external database beyond LittleHorse. **It is OK to use LittleHorse as a Data Store**. In fact, sometimes your job as an application developer becomes far easier when you do this.

### Required Systems

This application requires only one `TaskDef`: `send-email`, which is defined in the [`java-send-email`](../../common-tasks/java-send-email/) directory.

As such, to run the app, you need:
* LH Cluster (see the root readme for instructions)
* The `java-send-email` Task Worker
* The `workflow` program to register the `WfSpec`
* The `rest-api` to serve requests

### Set Up the App

First, as described in the [Root README](../../README.md), please set up the prerequisites.

#### Register TaskDef

Register the `send-email` `TaskDef`. From the root of the repo, please run:

```
./common-tasks/java-send-email/register.sh
```

You can confirm that the `TaskDef` exists via:
```
lhctl get taskDef send-email
```

Next, in a terminal please run the Task Worker:

```
./common-tasks/java-send-email/run-worker.sh
```

#### Register the Workflow Spec

From this directory, run:
```
./register.sh
```

You can confirm that the `WfSpec` was created by:

```
lhctl get wfSpec it-request
```

## Run the Workflow with `lhctl`

If you want to really know what's going on under the hood, you can run the application using `lhctl`.

### Run the `WfRun`

First, run the workflow. We will pass in a few input variables:

```
lhctl run it-request requester-email colt@littlehorse.io item-description "a nice laptop"
```

### Test Search Capabilities

We can search for IT Requests that were sent by `colt@littlehorse.io`. The following command should show a composite ID containing the `WfRun`'s id from before:

```
lhctl search variable --wfSpecName it-request --varType STR --name requester-email --value 'colt@littlehorse.io'
```

We can also search for `PENDING`, `APPROVED` or `REJECTED` IT Requests:

```
# Should be empty
-> lhctl search variable --wfSpecName it-request --varType STR --name status --value APPROVED

# Should contain the ID
-> lhctl search variable --wfSpecName it-request --varType STR --name status --value PENDING
```


### Assign the User Task Run

Next, search for a `UserTaskRun` in the `UNASSIGNED` state, which belongs to the `finance` department:

```
-> lhctl search userTaskRun --userTaskStatus UNASSIGNED --userGroup finance
{
  "results": [
    {
      "wfRunId": {
        "id": "f76af884642347d0b1af2600ee9fb06b"
      },
      "userTaskGuid": "f0ccec0e43d84cf9947af2c25cdc226e"
    }
  ]
}
```

Let's inspect that `UserTaskRun`. We can see that it is not assigned to anyone:
```
-> lhctl get userTaskRun f76af884642347d0b1af2600ee9fb06b f0ccec0e43d84cf9947af2c25cdc226e
```

Let's assign the `UserTaskRun` to a specific user (in this case, `yoda`).

```
lhctl assign userTaskRun f76af884642347d0b1af2600ee9fb06b f0ccec0e43d84cf9947af2c25cdc226e --userId yoda
```

### Check Search Again

Let's see if we can't find any `UserTaskRun`s assigned to `yoda`, can we?

```
-> lhctl search userTaskRun --status ASSIGNED --userId yoda
```

That command should show the composite `UserTaskRun` id from above.

### Execute the User Task

Now, to complete the workflow, we need to execute the User Task:

```
lhctl execute userTaskRun f76af884642347d0b1af2600ee9fb06b f0ccec0e43d84cf9947af2c25cdc226e
```

That command should walk you through executing the user task run. When asked for `user-id`, provide `yoda`. To approve the request, pass `true`; to reject it, pass `false`.

### Search One Last Time

You can search for Task Run's of the `send-email` task as follows:

```
lhctl search taskRun --status COMPLETED --taskDefName send-email
```

Then get the TaskRun as follows, and see the output:

```
lhctl get taskRun <wfRunId> <taskGuid>
```

## Run the Workflow With REST API

## Future Work and Extensions

We will add a primitive ReactJS frontend on top of the Spring Boot API in order to make the demo easier to run through.

There are many other ways to implement or extend this system. I have put some ideas below.

1. Use Python or Go to create the `WfSpec` as we do in the `workflow` sub-directory.
2. Use Python or Go to implement the `rest-api`.
3. Use the "Reminder" feature of User Tasks to send an email to the fictitious Finance Department if they have an IT request which has not been approved in over 5 minutes.
4. Store the status of the `it-request` Domain Object in a SQL database rather than using the `WfRunVariable`s.