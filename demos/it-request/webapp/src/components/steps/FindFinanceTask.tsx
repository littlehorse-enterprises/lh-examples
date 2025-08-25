'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';
import { Button } from '../ui/button';

export function FindFinanceTask() {
  const { wfRunId, currentStep, isLoading } = useWorkflowContext();
  const { findFinanceTask } = useWorkflowActions();
  const router = useRouter();
  const [financeTaskGuid, setFinanceTaskGuid] = useState<string | null>(null);

  useEffect(() => {
    const find = async () => {
      const guid = await findFinanceTask(); // TODO: add error handling
      if (guid) setFinanceTaskGuid(guid);
    };
    
    find();
  }, [wfRunId, setFinanceTaskGuid, findFinanceTask ]);

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