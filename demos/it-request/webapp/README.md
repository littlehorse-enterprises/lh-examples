# IT Request Workflow Demo

A Next.js application demonstrating LittleHorse User Tasks through an IT request approval workflow. The application guides users through a complete workflow where a requester submits an IT request and a finance team member reviews and approves or declines it.

## Architecture

The application uses Next.js 15 App Router with the following architecture:

- **URL-based state management**: Each workflow step is a separate route (`/workflow/[wfRunId]/step/[stepNumber]`)
- **Server Actions**: Direct server-side mutations without API routes
- **Context per page**: Ephemeral UI state using React Context API (no global state)
- **Type-safe gRPC client**: LittleHorse client for workflow operations

### Workflow Steps

1. **Health Check**: Verify API connectivity
2. **Start Workflow**: Initialize IT request with user ID
3. **Find Requesting Task**: Locate the user's task
4. **Complete Request**: Submit item and justification
5. **Find Finance Task**: Locate the finance review task
6. **Assign Finance Task**: Assign to a finance user
7. **Complete Finance Task**: Approve or decline the request

## Technologies

- **Next.js 15.1**: React framework with App Router
- **React 19**: UI library
- **TypeScript**: Type safety
- **TailwindCSS**: Utility-first CSS framework
- **LittleHorse Client**: gRPC client for workflow orchestration
- **Radix UI**: Accessible component primitives (checkbox, dialog, label, radio-group)
- **Lucide React**: Icon library
- **React Syntax Highlighter**: Code formatting for API responses

## Prerequisites

- Node.js 18+
- LittleHorse Server running locally or accessible
- Environment variables configured (see Configuration)

## Installation

```bash
npm install
```

## Configuration

Create a `.env.local` file with:

```env
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHC_API_PROTOCOL=PLAINTEXT
```

## Development

```bash
npm run dev
```

Opens the application at `http://localhost:3000`

## Production

```bash
npm run build
npm start
```

## Scripts

- `npm run dev` - Start development server with Turbopack
- `npm run build` - Build for production
- `npm start` - Start production server
- `npm run lint` - Check code formatting
- `npm run lint:fix` - Fix code formatting

## Project Structure

```
src/
├── app/
│   ├── actions/         # Server actions for LittleHorse operations
│   ├── api/             # Health check endpoint
│   └── workflow/        # App Router pages
│       └── [wfRunId]/
│           └── step/
│               └── [stepNumber]/
│                   └── page.tsx
├── components/
│   ├── providers/       # React Context providers
│   ├── steps/           # Step-specific components
│   └── ui/              # Reusable UI components
├── hooks/               # Custom React hooks
└── lib/                 # Utilities and client configuration
```

## License

See LICENSE file in the repository root.