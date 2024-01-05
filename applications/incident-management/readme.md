# IT Incident Management System

## Overview
Part of the `lh.quickstart` package, this project automates handling and resolving IT incidents like application downtimes, server failures, or network issues. It highlights the use of conditionals, loops, external events, and mutating variables in a real-world IT operational setting.

## Scenario Implementation

### 1. **Conditionals: Incident Priority Assessment**
- **Implementation**: The `verify-incident` task in `IncidentWorker.java` assesses incident priority based on severity. Higher severity incidents are marked as critical.
- **Workflow Integration**: `ConditionalsExample.java` uses conditional statements (`wf.doIf`) to execute specific tasks based on incident severity.

### 2. **Loops: Automated Troubleshooting Checks**
- **Implementation**: `periodic-check-task` in `IncidentWorker.java` performs ongoing status checks until the incident is resolved.
- **Workflow Integration**: `ConditionalsExample.java` implements a `doWhile` loop to execute the periodic check task continuously.

### 3. **External Events: Incident Resolution Trigger**
- **Implementation**: The project utilizes an external event, `incident-resolved`, to signal the resolution of an incident.
- **Workflow Integration**: In `ConditionalsExample.java`, the `registerInterruptHandler` method listens for the `incident-resolved` event. When triggered, it updates the `incidentResolved` variable, altering the workflow's execution path.

### 4. **Mutating Variables: Dynamic Incident Status Update**
- **Implementation**: The `incidentResolved` variable in `ConditionalsExample.java` is dynamically updated to reflect the incident's current state.
- **Workflow Integration**: The variable mutation demonstrates real-time changes in incident status within the workflow.

## Workflow Details
- **Incident Verification**: Examines the incident's type and severity for categorization.
- **Periodic Checks**: Repeatedly checks the incident status and issues alerts based on severity.
- **External Event Handling**: Listens for the `incident-resolved` event to update the incident's status.

## Project Components
- `App.java`: Manages the workflow and task workers, sets up the gRPC client, and handles external event registration.
- `ConditionalsExample.java`: Defines the workflow logic, incorporating conditionals, loops, and external event handling.
- `IncidentWorker.java`: Implements methods for verifying incidents, sending alerts, and conducting periodic checks.

## Running the Project
- **Build**: `./gradlew build`
- **Register Workflow**: `./gradlew run --args register`
- **Start Workflow**: `./gradlew run --args start`

## API Usage
- **Start Workflow Run**: `lhctl run it-incident severity [level] incidentDetails '[json]' resolved [true/false]`
- **Check Task Run**: `lhctl get taskRun [wfRunId] [taskGuid]`

## Future Enhancements
- Integration with IT management tools like JIRA and ServiceNow.
- Using APIs for notifications, such as emails and Slack messages.