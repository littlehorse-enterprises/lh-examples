# LittleHorse User Tasks Webapp (Workspace)

This workspace contains the frontend (React + Vite) and backend (Express) for the User Tasks example.

## Structure

- `frontend`: React + TypeScript app
- `backend`: Express + TypeScript API proxy to LittleHorse

## Requirements

- Node 18+

## Install

```bash
npm install
```

Installs dependencies for both `frontend` and `backend` via npm workspaces.

## Development

```bash
npm run dev
```

- Backend: `http://localhost:4000`
- Frontend: `http://localhost:5173`
- Vite dev server proxies requests from `/api/*` to `http://localhost:4000`, so no CORS or `VITE_API_URL` is needed in dev.

## Build

```bash
npm run build
```

Builds both packages.

## Start (backend only)

```bash
npm run start
```

Starts the backend from compiled output. If desired, you can serve the built frontend from the backend by statically serving `../frontend/dist` after running the frontend build.

## Notes

- The frontend still supports `VITE_API_URL` for custom API endpoints (e.g., when deploying separately). By default it uses relative URLs, and the dev proxy handles API calls during development.
- Each package keeps its own `package.json` and TypeScript configs for clear boundaries.
