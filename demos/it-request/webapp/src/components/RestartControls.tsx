import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowOperations } from '@/hooks/useWorkflowOperations';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export const RestartControls = () => {
  const { deleteScope, setDeleteScope } = useWorkflowStore();
  const { restart } = useWorkflowOperations();

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
        
        <Button onClick={restart} className="w-full">
          Restart Workflow
        </Button>
      </CardContent>
    </Card>
  );
};