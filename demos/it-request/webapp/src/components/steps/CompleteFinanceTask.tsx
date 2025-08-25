'use client';

import { useState } from 'react';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const CompleteFinanceTask = () => {
  const { isLoading, showResultModal, setShowResultModal, taskGuid, userId } = useWorkflowContext();
  const { completeFinanceTask } = useWorkflowActions();
  
  const [decision, setDecision] = useState<'APPROVE' | 'DECLINE'>('APPROVE');
  const [workflowComplete, setWorkflowComplete] = useState(false);

  const handleComplete = async () => {
    const success = await completeFinanceTask(decision, taskGuid!, userId!); // TODO: taskGuid should not be undefined at this point, handle this better. Also, add error handling
    if (success) {
      setWorkflowComplete(true);
      setShowResultModal(true);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center space-x-2">
        <Checkbox
          id="approve-checkbox"
          checked={decision === 'APPROVE'}
          onCheckedChange={(checked) => 
            setDecision(Boolean(checked) ? 'APPROVE' : 'DECLINE')
          }
          disabled={showResultModal || workflowComplete || isLoading}
        />
        <Label htmlFor="approve-checkbox">Approve IT request</Label>
      </div>
      <Button
        onClick={handleComplete}
        disabled={isLoading}
      >
        Submit Decision
      </Button>
    </div>
  );
};