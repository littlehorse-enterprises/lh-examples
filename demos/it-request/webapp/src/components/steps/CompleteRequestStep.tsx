'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowOperations } from '@/hooks/useWorkflowOperations';

export const CompleteRequestStep = () => {
  const { 
    requestedItem, 
    justification, 
    requestingTaskSubmitted,
    setRequestData,
    nextStep 
  } = useWorkflowStore();
  const { completeRequestingTask } = useWorkflowOperations();

  const canSubmit = requestedItem.trim().length > 0 && justification.trim().length > 0;

  const handleSubmit = async () => {
    if (canSubmit) {
      await completeRequestingTask();
    }
  };

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="requested-item">Requested Item</Label>
        <Input
          id="requested-item"
          type="text"
          required
          aria-required={true}
          value={requestedItem}
          onChange={(e) => setRequestData(e.target.value, justification)}
          placeholder="What do you need?"
          disabled={requestingTaskSubmitted}
        />
      </div>
      
      <div className="space-y-2">
        <Label htmlFor="justification">Justification</Label>
        <Input
          id="justification"
          type="text"
          required
          aria-required={true}
          value={justification}
          onChange={(e) => setRequestData(requestedItem, e.target.value)}
          placeholder="Why is it needed?"
          disabled={requestingTaskSubmitted}
        />
      </div>
      
      <Button
        onClick={requestingTaskSubmitted ? nextStep : handleSubmit}
        disabled={!requestingTaskSubmitted && !canSubmit}
      >
        Continue
      </Button>
    </div>
  );
};