'use client';

import { useEffect } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowOperations } from '@/hooks/useWorkflowOperations';

export const FindFinanceTaskStep = () => {
  const { financeUserTaskGuid } = useWorkflowStore();
  const { findFinanceTask } = useWorkflowOperations();

  useEffect(() => {
    findFinanceTask();
  }, [findFinanceTask]);

  return (
    <div className="space-y-2 text-sm">
      {financeUserTaskGuid && (
        <p><strong>Finance userTaskGuid:</strong> {financeUserTaskGuid}</p>
      )}
    </div>
  );
};