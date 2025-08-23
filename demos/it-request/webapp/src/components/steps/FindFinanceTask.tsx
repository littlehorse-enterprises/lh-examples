'use client';

import { useEffect } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const FindFinanceTask = () => {
  const { financeUserTaskGuid } = useWorkflowStore();
  const { findFinanceTask } = useWorkflowActions();

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