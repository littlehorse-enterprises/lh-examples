'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const StartWorkflow = () => {
  const [userId, setUserId] = useState('');
  const { isLoading, currentStep } = useWorkflowContext();
  const { runWorkflow } = useWorkflowActions(); 
  const router = useRouter();

  const isValidUserId = (userId: string): boolean => userId.trim().length >= 3; // TODO: review userId validation

  const handleSubmit = async () => {
    const wfRunId = await runWorkflow(userId);
    if (wfRunId) {
      router.push(`/workflow/${wfRunId}/step/${currentStep + 1}?userId=${userId}`);
    }
  };

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="requester-id">User ID</Label>
        <Input
          id="requester-id"
          type="text"
          required
          aria-required={true}
          aria-invalid={!isValidUserId(userId) && userId.trim().length > 0}
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          placeholder="Enter User ID"
          disabled={isLoading}
        />
      </div>
      <Button
        onClick={handleSubmit}
        disabled={!isValidUserId(userId) || isLoading}
      >
        Run IT workflow
      </Button>
    </div>
  );
};