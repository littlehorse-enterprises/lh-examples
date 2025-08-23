import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { useWorkflowStore } from '@/store/workflow.store';

export const CompleteFinanceTask = () => {
  const { 
    financeDecision, 
    showResultModal, 
    workflowCompleted,
    setFinanceDecision 
  } = useWorkflowStore();

  return (
    <div className="space-y-4">
      <div className="flex items-center space-x-2">
        <Checkbox
          id="approve-checkbox"
          checked={financeDecision === 'APPROVE'}
          onCheckedChange={(checked) => 
            setFinanceDecision(Boolean(checked) ? 'APPROVE' : 'DECLINE')
          }
          disabled={showResultModal || workflowCompleted}
        />
        <Label htmlFor="approve-checkbox">Approve IT request</Label>
      </div>
    </div>
  );
};