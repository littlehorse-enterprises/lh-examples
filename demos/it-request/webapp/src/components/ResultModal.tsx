'use client';

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';

interface ResultModalProps {
  isOpen: boolean;
  onClose: () => void;
  financeDecision: 'APPROVE' | 'DECLINE';
}

export function ResultModal({ isOpen, onClose, financeDecision }: ResultModalProps) {
  const message = financeDecision === 'APPROVE'
    ? 'The IT request has been approved. An email has been sent to the user.'
    : 'The IT request has been declined. An email has been sent to the user.';

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Result</DialogTitle>
          <DialogDescription className="text-lg py-4">
            {message}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button onClick={onClose}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}