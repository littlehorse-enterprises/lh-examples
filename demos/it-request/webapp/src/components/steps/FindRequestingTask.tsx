'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';
import { Button } from '@/components/ui/button';

export const FindRequestingTask = () => {
  const { wfRunId, userId, currentStep, isLoading } = useWorkflowContext();
  const { findRequestingTask } = useWorkflowActions();
  const router = useRouter();
  const [retrievedTaskGuid, setRetrievedTaskGuid] = useState<string | null>(null);

  useEffect(() => {
    if (wfRunId && userId && !retrievedTaskGuid) {
      const executeTask = async () => {
        const taskGuid = await findRequestingTask(userId);
        if (taskGuid) {
          setRetrievedTaskGuid(taskGuid);
        }
      };
      
      executeTask();
    }
  }, [wfRunId, userId, retrievedTaskGuid, findRequestingTask]);

  const handleContinue = () => {
    if (!retrievedTaskGuid || !wfRunId || !userId) return;
    
    const nextStep = currentStep + 1;
    const params = new URLSearchParams(window.location.search);
    
    params.set('userId', userId);
    params.set('taskGuid', retrievedTaskGuid);
    
    const url = `/workflow/${wfRunId}/step/${nextStep}?${params.toString()}`;
    router.push(url);
  };

  const canContinue = () => {
    return Boolean(retrievedTaskGuid && !isLoading);
  };

  return (
    <>
      <div className="space-y-2 text-sm">
        {wfRunId && (
          <p><strong>wfRunId:</strong> {wfRunId}</p>
        )}
        {retrievedTaskGuid && (
          <p><strong>userTaskGuid:</strong> {retrievedTaskGuid}</p>
        )}
      </div>
      
      <Button
        onClick={handleContinue}
        disabled={!canContinue()}
        className="w-full sm:w-auto"
      >
        Continue
      </Button>
    </>
  );
};