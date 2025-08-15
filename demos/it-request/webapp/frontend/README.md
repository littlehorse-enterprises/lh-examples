# LittleHorse User Tasks – IT Request (Frontend)

Simple React + TypeScript single-page UI for the User Tasks example. It guides you through seven steps (health check, start workflow, find/complete requesting task, find/assign/complete Finance task). It blocks the UI during API calls and shows only the current step’s response.

## Prerequisites
- Node 18+ (nvm recommended)
- Backend running (see ../backend)

## API URL
In development, the Vite dev server proxies `/api` to `http://localhost:4000`, so no configuration is required.
You can override with `VITE_API_URL` if deploying separately.

## Run (development)

Run from workspace root:

```bash
npm run dev -w frontend
```

Open the printed URL (usually `http://localhost:5173`). API calls to `/api/*` will be proxied to `http://localhost:4000` during development.

## Build and preview (optional)

```bash
npm run build
npm run preview
```

## What it does
- Step 1: Checks API health automatically. Enables Continue if healthy.
- Step 2: Starts the IT Request workflow with a validated User ID, stores `wfRunId`.
- Step 3: Finds your requesting task; if released, finds by definition and assigns to you.
- Step 4: Submits Requested Item and Justification.
- Step 5: Finds the Finance task.
- Step 6: Assigns Finance task to a user (optional override).
- Step 7: Records approve/decline decision and shows a final modal.