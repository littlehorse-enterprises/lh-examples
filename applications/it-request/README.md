# IT Request

- [IT Request](#it-request)
  - [Overview](#overview)
    - [Business Process](#business-process)
    - [Relevant Features](#relevant-features)
  - [For Developers](#for-developers)
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

TODO: Instructions on how to run the application.

## Future Work and Extensions

There are many other ways to implement or extend this system. I have put some ideas below.

1. Use Python or Go to create the `WfSpec` as we do in the `workflow` sub-directory.
2. Use Python or Go to implement the `rest-api`.
3. Use the "Reminder" feature of User Tasks to send an email to the fictitious Finance Department if they have an IT request which has not been approved in over 5 minutes.
4. Store the status of the `it-request` Domain Object in a SQL database rather than using the `WfRunVariable`s.