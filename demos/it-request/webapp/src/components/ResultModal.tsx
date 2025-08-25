'use client';

import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';

export function ResultModal() {
  const { showResultModal, setShowResultModal, financeDecision } = useWorkflowContext();

  const message = financeDecision === 'APPROVE'
    ? 'The IT request has been approved. An email has been sent to the user.'
    : 'The IT request has been declined. An email has been sent to the user.';

  return (
    <Dialog open={showResultModal} onOpenChange={setShowResultModal}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Result</DialogTitle>
          <DialogDescription className="text-lg py-4">
            {message}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button onClick={() => setShowResultModal(false)}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};