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
  const { wfRunId, isLoading, userId, taskGuid, currentStep, setStatus, setResponse } = useWorkflowContext();
  const { completeRequestingTask } = useWorkflowActions();
  const router = useRouter();
  const [item, setItem] = useState('');
  const [justification, setJustification] = useState('');

  const handleSubmit = async () => {
    if (!userId || !taskGuid) {
      setStatus('Error: Missing user ID or task GUID');
      setResponse(JSON.stringify({ error: 'userId and taskGuid are required' }, null, 2));
      return;
    }
    
    try {
      const success = await completeRequestingTask(userId, taskGuid, item, justification);
      if (success) {
        router.push(`/workflow/${wfRunId}/step/${currentStep + 1}`);
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      setStatus(`Failed to complete request: ${message}`);
      setResponse(JSON.stringify({ error: message }, null, 2));
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