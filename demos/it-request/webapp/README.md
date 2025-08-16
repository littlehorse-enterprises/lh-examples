# LittleHorse IT Request Demo - Next.js

This is a Next.js application that demonstrates LittleHorse User Tasks in a corporate IT request workflow. It showcases how to assign tasks to specific users or user groups, collect form data, and manage approval workflows.

## Features

- **Next.js 14** with App Router
- **TypeScript** for type safety
- **Tailwind CSS** for modern styling
- **Server Actions** for API calls
- **LittleHorse Client** integration
- **Responsive Design** with dark theme
- **Accessibility** features (ARIA labels, keyboard navigation, focus management)

## Prerequisites

- Node.js 18 or higher
- LittleHorse server running on localhost:2023 (or configured endpoint)
- Java application running the workflow definitions

## Getting Started

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Set up environment variables:**
   Create a `.env.local` file with:
   ```
   LHC_API_HOST=localhost
   LHC_API_PORT=2023
   LHC_API_PROTOCOL=PLAINTEXT
   ```

3. **Run the development server:**
   ```bash
   npm run dev
   ```

4. **Open your browser:**
   Navigate to [http://localhost:5173](http://localhost:5173)

## Architecture

This Next.js application uses:

- **Server Actions** (`lib/api.ts`) - Handle all LittleHorse client interactions
- **Types** (`lib/types.ts`) - TypeScript definitions using littlehorse-client types
- **Components** (`components/ITRequestApp.tsx`) - Main application component
- **Tailwind CSS** - For styling with custom design system

## Workflow Steps

1. **Health Check** - Verify API connectivity
2. **Start Workflow** - Create new IT request workflow
3. **Find User Task** - Locate requesting user task
4. **Complete Request** - Fill out item description and justification
5. **Find Finance Task** - Locate finance approval task
6. **Assign Finance Task** - Assign to finance user
7. **Complete Approval** - Approve or decline the request

## Development

### Building for Production

```bash
npm run build
npm start
```

### Linting

```bash
npm run lint
```

## Modern Architecture

This Next.js application provides a modern, production-ready foundation with:

- **Better TypeScript integration** with direct littlehorse-client type imports
- **Server Actions** for improved security and performance (no separate backend needed)
- **Built-in optimization** with Next.js (automatic code splitting, image optimization)
- **Modern development experience** with App Router and hot reload
- **Enhanced security** with server-side API calls and built-in CSRF protection

## Environment Variables

- `LHC_API_HOST` - LittleHorse server host (default: localhost)
- `LHC_API_PORT` - LittleHorse server port (default: 2023)
- `LHC_API_PROTOCOL` - Protocol to use (default: PLAINTEXT)
- `LHC_CA_CERT` - Optional CA certificate path

## Troubleshooting

### Common Issues

1. **API Connection Failed**
   - Ensure LittleHorse server is running
   - Check environment variables
   - Verify network connectivity

2. **Build Errors**
   - Run `npm install` to ensure dependencies are installed
   - Check TypeScript errors with `npm run lint`

3. **Workflow Not Found**
   - Ensure the Java application is running and has registered the workflow definitions
   - Check LittleHorse dashboard at http://localhost:8080

## Learn More

- [Next.js Documentation](https://nextjs.org/docs)
- [LittleHorse Documentation](https://littlehorse.io/docs)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
