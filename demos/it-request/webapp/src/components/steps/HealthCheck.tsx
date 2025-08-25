'use client';

import { useEffect } from 'react';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';

export const HealthCheck = () => {
  const { setLoading, setStatus, setResponse, wfRunId, isLoading } = useWorkflowContext();
  const router = useRouter();

  useEffect(() => {
    const checkHealth = async () => {
      setLoading(true);
      setStatus('Checking API health... Ensuring the backend is reachable.');
      setResponse('');

      try {
        const response = await fetch('/api/health');
        const data = await response.json();

        setResponse(JSON.stringify(data, null, 2));
        setStatus(data.ok ? 'API health check passed.' : 'API health check failed.');
        
        // Remove automatic redirect - let user manually continue
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unknown error';

        setResponse(JSON.stringify({ error: message }, null, 2));
        setStatus(`API health check failed: ${message}`);
      } finally {
        setLoading(false);
      }
    };

    checkHealth();
  }, []);

  const handleContinue = () => {
    router.push(`/workflow/${wfRunId || 'new'}/step/2`);
  };

  return (
    <div className="space-y-4">
      <div className="text-sm">Checking API connection...</div>
      {!isLoading && (
        <Button onClick={handleContinue} className="w-full sm:w-auto">
          Continue
        </Button>
      )}
    </div>
  );
};