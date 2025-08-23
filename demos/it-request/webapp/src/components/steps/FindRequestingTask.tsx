'use client';

import { useEffect } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const FindRequestingTask = () => {
  const { wfRunId, requestingUserTaskGuid } = useWorkflowStore();
  const { findRequestingTask } = useWorkflowActions();

  useEffect(() => {
    findRequestingTask();
  }, [findRequestingTask]);

  return (
    <div className="space-y-2 text-sm">
      {wfRunId && (
        <p><strong>wfRunId:</strong> {wfRunId.id}</p>
      )}
      {requestingUserTaskGuid && (
        <p><strong>userTaskGuid:</strong> {requestingUserTaskGuid}</p>
      )}
    </div>
  );
};