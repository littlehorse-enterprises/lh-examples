'use client';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';

import { HealthCheck } from './HealthCheck';
import { StartWorkflow } from './StartWorkflow';
import { FindRequestingTask } from './FindRequestingTask';
import { CompleteRequest } from './CompleteRequest';
import { FindFinanceTask } from './FindFinanceTask';
import { AssignFinanceTask } from './AssignFinanceTask';
import { CompleteFinanceTask } from './CompleteFinanceTask';

const stepInfo = {
  1: {
    title: 'Step 1: Check API health',
    description: 'API health check: Ensuring the backend is reachable.',
    component: HealthCheck,
  },
  2: {
    title: 'Step 2: Run IT Request workflow',
    description: 'Enter a valid User ID and start the IT Request workflow.',
    component: StartWorkflow,
  },
  3: {
    title: 'Step 3: Find requesting user task',
    description: 'First finds the task assigned to the requester. In this workflow, when the task is not claimed after 1 minute it gets released. In that case, it is found by definition and assigned back.',
    component: FindRequestingTask,
  },
  4: {
    title: 'Step 4: Complete requesting user task',
    description: 'Provide the Requested Item and Justification (both required), then submit to complete the requesting task.',
    component: CompleteRequest,
  },
  5: {
    title: 'Step 5: Find the Finance task',
    description: 'Finding the Finance task (automatically created by the workflow).',
    component: FindFinanceTask,
  },
  6: {
    title: 'Step 6: Assign Finance task to a user',
    description: 'Enter any Finance User ID to assign the task. Optionally enable override to take the task if needed.',
    component: AssignFinanceTask,
  },
  7: {
    title: 'Step 7: Complete Finance task',
    description: 'Choose to approve or decline the IT request, then submit the decision to finish.',
    component: CompleteFinanceTask,
  },
} as const;

export function StepContainer() {
  const { currentStep } = useWorkflowContext();

  const stepData = stepInfo[currentStep as keyof typeof stepInfo];
  const StepComponent = stepData.component;  

  return (
    <Card>
      <CardHeader>
        <CardTitle id="step-heading">{stepData.title}</CardTitle>
        <CardDescription>{stepData.description}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <StepComponent />
      </CardContent>
    </Card>
  );
}