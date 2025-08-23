import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const StartWorkflow = () => {
  const { requestingUserId, wfRunId, setRequestingUserId } = useWorkflowStore();
  const { runWorkflow } = useWorkflowActions();

  const isValidUserId = (userId: string): boolean => userId.trim().length >= 3;

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="requester-id">User ID</Label>
        <Input
          id="requester-id"
          type="text"
          required
          aria-required={true}
          aria-invalid={!isValidUserId(requestingUserId) && requestingUserId.trim().length > 0}
          value={requestingUserId}
          onChange={(e) => setRequestingUserId(e.target.value)}
          placeholder="Enter User ID"
          disabled={Boolean(wfRunId)}
        />
      </div>
      <Button
        onClick={runWorkflow}
        disabled={!isValidUserId(requestingUserId) || Boolean(wfRunId)}
      >
        Run IT workflow
      </Button>
    </div>
  );
};