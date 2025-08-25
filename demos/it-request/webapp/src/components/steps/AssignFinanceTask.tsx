'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const AssignFinanceTask = () => {
  const { wfRunId, isLoading, taskGuid, currentStep, setStatus, setResponse } = useWorkflowContext();
  const { assignFinanceTask } = useWorkflowActions();
  const router = useRouter();
  
  const [userId, setUserId] = useState('');
  const [override, setOverride] = useState(false);

  const handleAssign = async () => {
    if (!taskGuid) {
      setStatus('Error: Missing task GUID');
      setResponse(JSON.stringify({ error: 'taskGuid is required' }, null, 2));
      return;
    }
  
    try {
      const success = await assignFinanceTask(userId, override, taskGuid);
      if (success) {
        router.push(`/workflow/${wfRunId}/step/${currentStep + 1}?taskGuid=${taskGuid}&userId=${userId}`);
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      setStatus(`Failed to assign task: ${message}`);
      setResponse(JSON.stringify({ error: message }, null, 2));
    }
  };

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="finance-user-id">User ID</Label>
        <Input
          id="finance-user-id"
          type="text"
          required
          aria-required={true}
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          placeholder="Enter User ID"
          disabled={isLoading}
        />
      </div>
      
      <div className="flex items-center space-x-2">
        <Checkbox
          id="finance-override"
          checked={override}
          onCheckedChange={(checked) => setOverride(Boolean(checked))}
          disabled={isLoading}
        />
        <Label htmlFor="finance-override">Enable override</Label>
      </div>
      
      <Button
        onClick={handleAssign}
        disabled={!userId.trim() || isLoading} // TODO: review userId validation
      >
        Assign task
      </Button>
    </div>
  );
};