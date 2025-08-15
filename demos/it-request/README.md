# IT Request Demo

This demo showcases LittleHorse User Tasks in a corporate IT request workflow. It demonstrates how to assign tasks to specific users or user groups, collect form data, and manage approval workflows in a realistic business scenario.

## System Architecture

The application consists of the following components:

- **Java Application**: Handles workflow definition, task workers, and business logic
- **Frontend** (Port 5173): React + TypeScript web interface for creating and managing IT requests
- **Backend API** (Port 4000): Express + TypeScript API proxy to LittleHorse
- **LittleHorse Server** (Port 2023): Workflow orchestration engine
- **Kafka Broker** (Port 9092): Message streaming platform (embedded in LittleHorse)
- **LittleHorse Dashboard** (Port 8080): Web UI for monitoring workflows

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Node.js 18 or higher
- npm (comes with Node.js)

### Java Setup

Ensure you have Java 17 or higher installed:

```bash
java --version
# Should output Java 17 or higher
```

### Node.js Setup

The webapp requires Node.js 18+. You can install and manage Node.js versions using the `n` version manager:

1. Install `n` via Homebrew if you don't have it already:
   ```bash
   brew install n
   ```

2. Install and use Node.js 18 or higher:
   ```bash
   n 18
   ```

3. Verify the correct Node.js version:
   ```bash
   node --version
   # Should output v18.x.x or higher
   ```

## LittleHorse Dependency

This demo uses the standalone LittleHorse Docker image which includes an embedded Kafka cluster, eliminating the need for external dependencies. The configuration uses standard ports:

- **LittleHorse Server**: Port 2023
- **Kafka Broker**: Port 9092  
- **LittleHorse Dashboard**: Port 8080

The standalone image will automatically start when you run the demo. If you have any services already running on these ports, the startup script will detect them and ask you to stop them first.

## Running the Demo

### Quick Start (All Services)

Run the commands below to start the demo with the embedded LittleHorse standalone server:

```bash
# Start all services (LittleHorse, Java app, and webapp)
./start_demo.sh
```

### Alternative: External LittleHorse Server Mode

If you prefer to run your own LittleHorse server (e.g., for development), you can use the `--no-server` flag:

```bash
# Start only Java app and webapp (no LittleHorse container)
./start_demo.sh --no-server
```

**Prerequisites for `--no-server` mode:**
- You must have a LittleHorse server running on `localhost:2023`
- You must have Kafka running on `localhost:9092`
- Example: Run LittleHorse locally with `./local-dev/do-server.sh` from the LittleHorse repository

### Script Options

```bash
# Show help
./start_demo.sh --help

# Start with embedded LittleHorse (default)
./start_demo.sh

# Start without LittleHorse container (use external server)
./start_demo.sh --no-server
```

After starting the services, you can access:
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:4000
- **LittleHorse Dashboard**: http://localhost:8080

Once you're done with the demo, you can shut down all services with:

```bash
# Stop all services
./kill_services.sh
```

### Development Mode (Individual Services)

For development purposes, you may want to start services individually. Follow this order:

1. **Start LittleHorse server:**
   ```bash
   docker compose up -d littlehorse
   ```

2. **Build all components:**
   ```bash
   ./build_all.sh
   ```

3. **Start the Java application (registers tasks and starts task worker):**
   ```bash
   ./gradlew run
   ```

4. **Start the webapp (in a new terminal):**
   ```bash
   cd webapp
   npm run dev
   ```

## User Flow

### Web Interface Usage

1. **Access the Application**: Open http://localhost:5173 in your browser
2. **Create IT Request**: Use the web interface to submit a new IT request with item description and justification
3. **Monitor Progress**: View the request status and workflow progress in the LittleHorse Dashboard at http://localhost:8080
4. **Approval Process**: The request will be assigned to the finance user group for approval
5. **Complete Workflow**: Approve or reject the request to complete the workflow

## Business Logic

This example mimics a common corporate workflow in which an employee requests an item from the IT Department, and the Finance Department must approve the purchase request first. The workflow steps are:

1. **Initial Request**: A User Task is assigned to the `user-id` who initiated the workflow, involving filling out a description of the requested item and a justification.
2. **Finance Approval**: A User Task is assigned to the `finance` User Group. The task contains notes from the initial request and has one field: a boolean determining whether the purchase is approved.
3. **Notification**: If the purchase is approved, the requester is notified of approval.

## User Tasks Features

User Tasks are a type of `Node` in LittleHorse which allow you to assign a task (in this case, filling out a form) to a human. User Tasks have the following features:

- Can be assigned to a specific User ID or a User Group
- Produce output that can be saved into a Workflow Run `Variable` and used elsewhere in the `WfRun`
- Reminder Tasks are supported (not included in this example)
- Can include notes which are presented to the person executing the User Task

For more information about User Tasks, please consult the [User Task Documentation](https://littlehorse.io/docs/server/concepts/user-tasks) on our website.

## Advanced Usage with lhctl

### Manual Workflow Execution

You can also run workflows manually using `lhctl`:

```bash
# Start a workflow with a specific user ID
lhctl run it-request user-id anakin

# Check workflow status
lhctl get wfRun <wf_run_id>

# Search for user tasks
lhctl search userTaskRun --userId anakin
lhctl search userTaskRun --userGroup finance --userTaskStatus UNASSIGNED

# Execute user tasks manually
lhctl execute userTaskRun <wfRunId> <userTaskGuid>

# Assign user tasks to specific users
lhctl assign userTaskRun <wfRunId> <userTaskGuid> --userId 'mace'
```

### Finding User Task Runs

There are two general ways to find User Task Runs:

1. **Using SearchUserTaskRun RPC**:
   ```bash
   lhctl search userTaskRun --userId anakin
   lhctl search userTaskRun --userId anakin --userTaskStatus ASSIGNED
   lhctl search userTaskRun --userTaskStatus ASSIGNED --userTaskDefName it-request
   ```

2. **Looking at NodeRun**:
   ```bash
   # Get NodeRun (provide wfRunId, threadRun number, and nodeRun position)
   lhctl get nodeRun <wfRunId> 0 1
   ```

### Example Workflow Execution

1. **Create Request** (as user "anakin"):
   ```bash
   lhctl execute userTaskRun <wfRunId> <userTaskGuid>
   # Enter: user-id: anakin
   # Enter: description: the rank of master  
   # Enter: justification: it's not fair to be on this council and not be a Master!
   ```

2. **Finance Approval** (assign to "mace" and approve/reject):
   ```bash
   lhctl assign userTaskRun <wfRunId> <userTaskGuid> --userId 'mace'
   lhctl execute userTaskRun <wfRunId> <userTaskGuid>
   # Enter approval decision (true/false)
   ```

## Webapp Structure

The webapp is organized as an npm workspace containing:

- **Frontend**: React + TypeScript app (Vite) at http://localhost:5173
- **Backend**: Express + TypeScript API proxy to LittleHorse at http://localhost:4000

### Webapp Development

```bash
# Install dependencies
cd webapp
npm install

# Start development servers (both frontend and backend)
npm run dev

# Build both packages
npm run build

# Start only backend (production)
npm run start
```

### Webapp Configuration

- The Vite dev server proxies requests from `/api/*` to `http://localhost:4000`
- No CORS configuration needed in development
- Frontend supports `VITE_API_URL` for custom API endpoints when deploying separately
- Each package maintains its own `package.json` and TypeScript configs

## Troubleshooting

### Port Conflicts
If you encounter port conflicts, use the cleanup script:
```bash
./kill_services.sh
```

### Build Issues
Ensure all prerequisites are installed and try rebuilding:
```bash
./build_all.sh
```

### Docker Issues
Clean up Docker resources:
```bash
docker compose down
docker system prune
```

## Production Notes

In production environments:
- Users would log into a customized web frontend to execute User Task Runs
- LittleHorse tracks User Task state but doesn't provide a built-in web interface
- Custom presentation layers can be built for mobile apps, internal tools, or customer-facing applications
- For custom web frontends, contact LittleHorse Professional Services (`sales@littlehorse.io`)