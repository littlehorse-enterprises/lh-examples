'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { Loader2 } from 'lucide-react';

export function StatusPanel() {
  const { statusText, isLoading } = useWorkflowContext();

  return (
    <Card className="mb-6">
      <CardHeader className="mb-3">
        <CardTitle>Status</CardTitle>
      </CardHeader>
      <CardContent>
        <div 
          className="bg-muted rounded-md p-4 text-sm flex items-center gap-3"
          role="status"
          aria-live="polite"
        >
          {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
          <p className="whitespace-pre-wrap">{statusText}</p>
        </div>
      </CardContent>
    </Card>
  );
}