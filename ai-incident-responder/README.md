# LittleHorse AI Incident Responder

This example demonstrates a workflow that uses an AI agent to respond to system incidents with proper orchestration for resilience and reliability.

## Overview

This application showcases two approaches to incident response with AI:

1. **Direct AI Integration (Without LittleHorse)**: Shows how direct calls to AI services can fail catastrophically due to service unreliability
2. **Using LittleHorse Orchestration**: Demonstrates how LittleHorse provides resilience through retries, error handling, and workflow management

The application simulates an incident response pipeline with steps for detection, diagnosis, auto-remediation, and escalation - all with simulated failures to demonstrate resilience patterns.

## Running the Example

1. Choose which demo to run:

   - **To run both demos (default)**:

     ```bash
     ./gradlew run
     ```

   - **To run only the Direct AI demo**:

     ```bash
     ./gradlew run --args="direct"
     ```

     This will run the direct AI demo, which shows what happens when calling AI services directly without proper orchestration.

   - **To run only the LittleHorse orchestrated demo**:

     ```bash
     ./gradlew run --args="lh"
     ```

     This will run the LittleHorse orchestrated demo, which registers the metadata, starts the task workers, and runs 3 incidents with the workflow.

## What You'll See

### Demo 1: Direct AI Agent (Without LittleHorse)

This demo shows what happens when calling AI services directly without proper orchestration:

- Random failures cause complete pipeline breakdown
- No retries when LLM services are temporarily unavailable
- When a step fails, the entire incident response process crashes

### Demo 2: LittleHorse Orchestrated Workflow

This demo shows how LittleHorse brings resilience to the process:

- Automatic retries when services fail
- Proper error handling and fallback paths
- Ability to pause and wait for human intervention when needed
- End-to-end process visibility and monitoring

## Key Resilience Patterns

- Automatic retries with configurable policies
- Error handling with custom strategies
- Conditional flows based on incident severity and diagnosis
- Escalation paths for unresolved incidents
- Timeouts with reminder mechanisms

## Components

- **IncidentResponseWorkflow**: Defines the workflow with nodes for each processing step
- **Task Implementations**:
  - `DiagnoseIncidentTask`: Uses simulated LLM to analyze logs and metrics
  - `ValidateIncidentTask`: Validates incident information
  - `AttemptRemediationTask`: Uses simulated LLM to determine and execute remediation steps
  - `EscalateToEngineerTask`: Routes incidents to appropriate engineering teams
  - `NotifyStatusTask`: Notifies stakeholders about incident status
- **LHConstants**: Central repository for task names, and status codes

## Failure Simulation

All tasks randomly fail to simulate real-world API unreliability:

- Standard tasks: ~33% failure rate
- AI/LLM services: ~40% failure rate

This helps demonstrate how LittleHorse provides resilience in the face of unreliable services.
