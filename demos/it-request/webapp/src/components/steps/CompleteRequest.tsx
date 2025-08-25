'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const CompleteRequest = () => {
  const { wfRunId, isLoading, userId, taskGuid, currentStep } = useWorkflowContext();
  const { completeRequestingTask } = useWorkflowActions();
  const router = useRouter();
  const [item, setItem] = useState('');
  const [justification, setJustification] = useState('');

  const handleSubmit = async () => { // TODO: add error handling
    const success = await completeRequestingTask(userId!, taskGuid!, item, justification); // TODO: userId and taskGuid should not be null at this point, handle this better
    if (success) {
      router.push(`/workflow/${wfRunId}/step/${currentStep + 1}`);
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
          value={item}
          onChange={(e) => setItem(e.target.value)}
          placeholder="What do you need?"
          disabled={isLoading}
        />
      </div>
      
      <div className="space-y-2">
        <Label htmlFor="justification">Justification</Label>
        <Textarea
          id="justification"
          rows={4}
          required
          aria-required={true}
          value={justification}
          onChange={(e) => setJustification(e.target.value)}
          placeholder="Why is it needed?"
          disabled={isLoading}
        />
      </div>
      
      <Button
        onClick={handleSubmit}
        disabled={!item.trim() || !justification.trim() || isLoading}
      >
        Continue
      </Button>
    </div>
  );
};