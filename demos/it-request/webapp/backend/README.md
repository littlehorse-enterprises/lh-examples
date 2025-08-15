# LittleHorse User Tasks – IT Request (Backend)

Minimal Express server that proxies to the LittleHorse client. Endpoints used by the frontend:

- `GET /api/health`
- `POST /api/run/it-request` – starts workflow with `{ userId }`
- `GET /api/user-tasks` – query: `userId`, `userGroup`, `status`, `userTaskDefName`
- `GET /api/user-task/:wfRunId/:userTaskGuid`
- `POST /api/user-task/:wfRunId/:userTaskGuid/assign`
- `POST /api/user-task/:wfRunId/:userTaskGuid/complete`

## Prerequisites
- Node 18+
- LittleHorse server reachable via the client in `src/lh.ts`

## Run (development)
Run from the workspace root:
```bash
npm run dev -w backend
```
Serves at `http://localhost:4000` by default. CORS is not required when using the Vite dev proxy.

## Build and start (optional)
```bash
npm run build
npm start
```

## Notes
 - Uses Vite dev proxy in the frontend; no CORS needed in dev
- See `docs/user-tasks-simple-backend.postman_collection.json` for example requests


