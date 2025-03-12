# LittleHorse AI Document Processing

This example demonstrates a workflow that uses an AI agent to process documents with proper orchestration for resilience and reliability.

https://github.com/user-attachments/assets/08a76e2d-3214-4e5c-b73a-a6282750232d

## Overview

This application showcases two approaches to document processing with AI:

1. **Direct AI Integration (Without LittleHorse)**: Shows how direct calls to AI services can fail catastrophically due to service unreliability
2. **Using LittleHorse Orchestration**: Demonstrates how LittleHorse provides resilience through retries, error handling, and workflow management

The application simulates a document processing pipeline with steps for extraction, validation, AI-based routing, and notifications - all with simulated failures to demonstrate resilience patterns.

## Running the Example

1. Register metadata and start the task workers:

2. Choose which demo to run:

   - **To run both demos (default)**:

     ```bash
     ./gradlew run
     ```

   - **To run only the Direct AI Agent demo**:

     ```bash
     ./gradlew run --args direct
     ```

   - **To run only the LittleHorse Orchestrated demo**:

     ```bash
     ./gradlew run --args lh
     ```

3. Watch the console output to observe the differences between the approaches

4. To see the workflow executions in the LittleHorse dashboard go to [http://localhost:8080/](http://localhost:8080/)

## Understanding the Demos

### Demo 1: Direct AI Agent (Without LittleHorse)

Shows how traditional integration with AI services can fail:

- Processing stops at the first error
- No automatic retries
- No fallback mechanisms
- Complete process failure on any step failure

### Demo 2: AI Agent (With LittleHorse)

Shows how LittleHorse provides resilience:

- Automatic retries for failed tasks
- Strategic error handling
- Conditional flows based on document validity
- External event integration for human approval
- Timeouts with reminder mechanisms

## Components

- **DocumentProcessingWorkflow**: Defines the workflow with nodes for each processing step
- **Task Implementations**:
  - `ExtractDocumentInfoTask`: Uses simulated LLM to extracts information from documents
  - `ValidateDocumentTask`: Validates document structure and content
  - `DetermineApprovalRouteTask`: Uses simulated LLM to determine where to route the document
  - `RouteToDeprtmentTask`: Routes documents to appropriate departments
  - `NotifySubmitterTask`: Notifies original document submitter of status
- **LHConstants**: Central repository for task names, and status codes

## Failure Simulation

All tasks randomly fail to simulate real-world API unreliability:

- Standard tasks: ~33% failure rate
- AI/LLM services: ~40% failure rate

This helps demonstrate how LittleHorse provides resilience in the face of unreliable services.
