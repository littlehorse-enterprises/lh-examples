import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowActions } from '@/hooks/useWorkflowActions';

export const AssignFinanceTask = () => {
  const { 
    financeAssigneeUserId, 
    financeOverride, 
    financeAssigned,
    setFinanceAssignee,
    setFinanceOverride
  } = useWorkflowStore();
  const { assignFinanceTask } = useWorkflowActions();

  const isValidUserId = (userId: string): boolean => userId.trim().length >= 3;

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="finance-user-id">User ID</Label>
        <Input
          id="finance-user-id"
          type="text"
          required
          aria-required={true}
          value={financeAssigneeUserId}
          onChange={(e) => setFinanceAssignee(e.target.value)}
          placeholder="Enter User ID"
          disabled={financeAssigned}
        />
      </div>
      
      <div className="flex items-center space-x-2">
        <Checkbox
          id="finance-override"
          checked={financeOverride}
          onCheckedChange={(checked) => setFinanceOverride(Boolean(checked))}
          disabled={financeAssigned}
        />
        <Label htmlFor="finance-override">Enable override</Label>
      </div>
      
      <Button
        onClick={assignFinanceTask}
        disabled={!isValidUserId(financeAssigneeUserId) || financeAssigned}
      >
        Assign task
      </Button>
    </div>
  );
};