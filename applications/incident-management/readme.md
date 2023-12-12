# IT Incident Management System

## Overview
This project automates the handling and resolution of IT incidents, such as application downtimes, server failures, or network issues. It's part of the `lh.quickstart` package and demonstrates the use of conditionals, loops, mutating variables, and exception handling in a real-world IT operational context.

## Scenario Implementation

### 1. **Conditionals: Incident Priority Assessment**
- **Implementation**: The `verify-incident` task in `IncidentWorker.java` assesses incident priority. It uses conditionals to categorize incidents based on severity. High severity incidents (severity level >= 5) are marked as critical.
- **Workflow Integration**: The `QuickstartWorkflow.java` file uses a conditional statement (`wf.doIf`) to determine if the incident is critical and requires immediate action.

### 2. **Loops: Automated Troubleshooting Checks**
- **Implementation**: The `periodic-check-task` in `IncidentWorker.java` demonstrates a loop that continuously checks the status of an incident until it is resolved.
- **Workflow Integration**: The `doWhile` loop in `QuickstartWorkflow.java` repeatedly executes the periodic check task as long as the incident is not resolved (`incidentResolved` is false).

### 3. **Mutating Variables: Updating Incident Status**
- **Implementation**: In `QuickstartWorkflow.java`, we mutate the `incidentResolved` variable using an interrupt handler. When the `incident-resolved` event is triggered, the variable is updated to reflect the resolution of the incident.
- **Workflow Integration**: The mutation occurs within the `registerInterruptHandler` method, showcasing how the incident's status is dynamically updated.

### 4. **Exception Handling: Escalation Procedure**
- **Implementation**: Exception handling is demonstrated in `App.java` where it catches `StatusRuntimeException` during the external event registration. This handles scenarios where the event might already exist or other exceptions occur.
- **Workflow Integration**: While not directly integrated into the workflow, this exception handling is crucial for the robustness of the system setup and initialization.

## Workflow Details
- **Incident Verification**: Verifies the incident's type and severity, checking against known issues.
- **Periodic Checks**: Continuously checks the status of an incident and sends alerts based on its severity until it's resolved.

## Project Components

- `App.java`: Manages workflow and task workers, handles gRPC client setup, and external event registration.
- `QuickstartWorkflow.java`: Defines the workflow logic, including conditionals and loops.
- `IncidentWorker.java`: Contains methods for incident verification, sending alerts, and performing periodic checks.

## Running the Project

- **Build**: `./gradlew build`
- **Register Workflow**: `./gradlew run --args register`
- **Start Workflow**: `./gradlew run --args start`

## API Usage

- **Start Workflow Run**: `lhctl run quickstart severity [level] incidentDetails '[json]' resolved [true/false]`
- **Check Task Run**: `lhctl get taskRun [wfRunId] [taskGuid]`

## Future Enhancements

- Integration with IT management tools (JIRA, ServiceNow).
- API usage for notifications (emails, Slack messages).

