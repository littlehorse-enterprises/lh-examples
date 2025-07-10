# LittleHorse Bank Transfer Demo

## Overview

This demo implements a bank transfer workflow that:

1. Validates account details for source and destination accounts
2. Checks available balance
3. Initiates the transfer
4. Monitors transfer status
5. Handles failures with compensating transactions

The workflow demonstrates key LittleHorse features like:

- Task orchestration across multiple services
- Error handling and transaction rollback (SAGA pattern)
- User task integration for manual approvals
- Observability and tracing

## Prerequisites

- Java 11 or higher
- Docker and Docker Compose
- LittleHorse CLI (`lhctl`)

## Getting Started

1. Install the LittleHorse CLI:

```bash
brew install littlehorse-enterprises/littlehorse/lhctl
```

2. Clone the repository:

```bash
git clone https://github.com/littlehorse-enterprises/lh-bank-transfer-demo.git
cd lh-bank-transfer-demo
```

3. Build and register the workflow:

```bash
cd lh-bank-transfer
./gradlew build
./gradlew run --args register
```

4. Start the task workers:

```bash
java -jar app/build/libs/app.jar fetch-account
java -jar app/build/libs/app.jar initiate-transfer
java -jar app/build/libs/app.jar check-transfer
```

5. Start the mock accounts API:

```bash
cd ../accountsApi
./gradlew build
java -jar app/build/libs/app.jar
```

## Running a Transfer

Execute a transfer workflow using `lhctl`:

```bash
lhctl run initiate-transfer transferDetails '{
    "fromAccountId": "1234",
    "toAccountId": "4564",
    "amount": 500.23,
    "currency": "USD",
    "description": "Test transfer"
}'
```

Monitor the workflow progress in the LittleHorse Dashboard at `http://localhost:3000`.

## Project Structure

- `/lh-bank-transfer` - Core workflow implementation and task workers
- `/accountsApi` - Mock API service for account management

## Development

For local development:

1. Follow the setup instructions above
2. Make changes to the workflow in `lh-bank-transfer/app/src/main/java/io/littlehorse`
3. Rebuild and re-register the workflow after changes
