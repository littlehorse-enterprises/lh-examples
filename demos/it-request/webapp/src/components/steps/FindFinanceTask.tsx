'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';
import { Button } from '../ui/button';

export function FindFinanceTask() {
  const { wfRunId, currentStep, isLoading, setStatus, setResponse } = useWorkflowContext();
  const { findFinanceTask } = useWorkflowActions();
  const router = useRouter();
  const [financeTaskGuid, setFinanceTaskGuid] = useState<string | null>(null);

  useEffect(() => {
    const find = async () => {
      try {
        const guid = await findFinanceTask();
        if (guid) setFinanceTaskGuid(guid);
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unknown error';
        setStatus(`Failed to find finance task: ${message}`);
        setResponse(JSON.stringify({ error: message }, null, 2));
      }
    };
    
    find();
  }, [wfRunId]);

  const handleContinue = () => {
    router.push(`/workflow/${wfRunId}/step/${currentStep + 1}?taskGuid=${financeTaskGuid}`);
  };

  return (
    <>
    <div className="space-y-2 text-sm">
      {financeTaskGuid && <p><strong>Finance Task GUID:</strong> {financeTaskGuid}</p>}
    </div>
    <Button
      onClick={handleContinue}
      disabled={!financeTaskGuid || isLoading}
      className="w-full sm:w-auto"
    >
      Continue
    </Button>
    </>
  );
}