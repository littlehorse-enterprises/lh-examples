'use client';

import { useEffect } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';

type HealthResponse = { ok: boolean };

export const HealthCheckStep = () => {
  const { setApiHealth, setLoading, setStatus, setResponse } = useWorkflowStore();

  useEffect(() => {
    const checkHealth = async () => {
      setLoading(true);
      setStatus('Checking API health... Ensuring the backend is reachable.');
      setResponse('');

      try {
        const response = await fetch('/api/health');
        const data: HealthResponse = await response.json();

        setResponse(JSON.stringify(data, null, 2));
        setStatus(data.ok ? 'API health check passed.' : 'API health check failed.');
        setApiHealth(data.ok);
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unknown error';

        setResponse(JSON.stringify({ error: message }, null, 2));
        setStatus(`API health check failed: ${message}`);
        setApiHealth(false);
      } finally {
        setLoading(false);
      }
    };

    checkHealth();
  }, [setApiHealth, setLoading, setStatus, setResponse]);

  return null; // This is a headless component, no UI needed
};