'use client';

import { useState } from 'react';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

import { RestartControls } from '../RestartControls';
import { ResultModal } from '../ResultModal';

export const CompleteFinanceTask = () => {
  const { isLoading, taskGuid, userId } = useWorkflowContext();
  const { completeFinanceTask } = useWorkflowActions();
  
  const [decision, setDecision] = useState<'APPROVE' | 'DECLINE'>('APPROVE');
  const [workflowComplete, setWorkflowComplete] = useState(false);
  const [showResultModal, setShowResultModal] = useState(false);

  const handleComplete = async () => {
    const success = await completeFinanceTask(decision, taskGuid!, userId!);
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
        disabled={isLoading || showResultModal || workflowComplete}
      >
        Submit Decision
      </Button>
      {!showResultModal && workflowComplete && (
        <RestartControls />
      )}
      <ResultModal 
        isOpen={showResultModal}
        onClose={() => setShowResultModal(false)}
        financeDecision={decision}
      />
    </div>
  );
};