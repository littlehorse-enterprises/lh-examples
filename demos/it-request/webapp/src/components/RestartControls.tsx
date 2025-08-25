'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';

import { deleteWfRun } from '@/app/actions/deleteWfRun';
import { deleteAllWfRunsForSpec } from '@/app/actions/deleteAllWfRunsForSpec';

export const RestartControls = () => {
  const [deleteScope, setDeleteScope] = useState<'none' | 'current' | 'all'>('none');
  const { wfRunId, setLoading, setStatus, setResponse } = useWorkflowContext();
  const router = useRouter();

  const handleRestart = async () => {
    setLoading(true);
    
    try {
      if (deleteScope === 'current' && wfRunId) {
        await deleteWfRun({ id: wfRunId });

        setStatus('Deleted current wfRun.');
        setResponse(JSON.stringify({ deleted: 'current' }, null, 2));
      } else if (deleteScope === 'all') {
        const result = await deleteAllWfRunsForSpec('it-request');

        setStatus(`Deleted ${result.deleted} wfRun(s) for spec "it-request".`);
        setResponse(JSON.stringify(result, null, 2));
      }

      setTimeout(() => {
        router.push('/workflow/new/step/1');
      }, 1500);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setStatus(`Failed to restart: ${message}`);
      setResponse(JSON.stringify({ error: message }, null, 2));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card className="mt-6">
      <CardHeader>
        <CardTitle className="text-lg">Restart Options</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <RadioGroup 
          value={deleteScope} 
          onValueChange={(value) => setDeleteScope(value as 'none' | 'current' | 'all')}
        >
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="none" id="delete-none" />
            <Label htmlFor="delete-none">Keep all workflow runs</Label>
          </div>
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="current" id="delete-current" />
            <Label htmlFor="delete-current">Delete current wfRun</Label>
          </div>
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="all" id="delete-all" />
            <Label htmlFor="delete-all">Delete all wfRuns for this spec</Label>
          </div>
        </RadioGroup>
        
        <Button onClick={handleRestart} className="w-full">
          Restart Workflow
        </Button>
      </CardContent>
    </Card>
  );
};