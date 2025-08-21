'use client';

import { useMemo, useRef, useEffect } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';
import { useWorkflowOperations } from '@/hooks/useWorkflowOperations';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { darcula } from 'react-syntax-highlighter/dist/esm/styles/prism';

import { HealthCheckStep } from './steps/HealthCheckStep';
import { StartWorkflowStep } from './steps/StartWorkflowStep';
import { FindRequestingTaskStep } from './steps/FindRequestingTaskStep';
import { CompleteRequestStep } from './steps/CompleteRequestStep';
import { FindFinanceTaskStep } from './steps/FindFinanceTaskStep';
import { AssignFinanceTaskStep } from './steps/AssignFinanceTaskStep';
import { CompleteFinanceTaskStep } from './steps/CompleteFinanceTaskStep';

import { RestartControls } from './RestartControls';
import { ResultModal } from './ResultModal';

const stepInfo = {
  1: {
    title: 'Step 1: Check API health',
    description: 'API health check: Ensuring the backend is reachable.',
  },
  2: {
    title: 'Step 2: Run IT Request workflow',
    description: 'Enter a valid User ID and start the IT Request workflow.',
  },
  3: {
    title: 'Step 3: Find requesting user task',
    description: 'First finds the task assigned to the requester. In this workflow, when the task is not claimed after 1 minute it gets released. In that case, it is found by definition and assigned back.',
  },
  4: {
    title: 'Step 4: Complete requesting user task',
    description: 'Provide the Requested Item and Justification (both required), then submit to complete the requesting task.',
  },
  5: {
    title: 'Step 5: Find the Finance task',
    description: 'Finding the Finance task (automatically created by the workflow).',
  },
  6: {
    title: 'Step 6: Assign Finance task to a user',
    description: 'Enter any Finance User ID to assign the task. Optionally enable override to take the task if needed.',
  },
  7: {
    title: 'Step 7: Complete Finance task',
    description: 'Choose to approve or decline the IT request, then submit the decision to finish.',
  },
};

const StepRenderer = ({ step }: { step: number }) => {
  switch (step) {
    case 1:
      return <HealthCheckStep />;
    case 2:
      return <StartWorkflowStep />;
    case 3:
      return <FindRequestingTaskStep />;
    case 4:
      return <CompleteRequestStep />;
    case 5:
      return <FindFinanceTaskStep />;
    case 6:
      return <AssignFinanceTaskStep />;
    case 7:
      return <CompleteFinanceTaskStep />;
    default:
      return null;
  }
};

export const ITRequestFlow = () => {
  const headingRef = useRef<HTMLHeadingElement>(null);
  const {
    currentStep,
    apiHealthy,
    wfRunId,
    requestingUserTaskGuid,
    requestedItem,
    justification,
    requestingTaskSubmitted,
    financeUserTaskGuid,
    financeAssigned,
    showResultModal,
    workflowCompleted,
    isLoading,
    statusText,
    responseText,
    setStatus,
    nextStep,
  } = useWorkflowStore();
  
  const { completeFinanceTask } = useWorkflowOperations();

  const canContinue = useMemo(() => {
    switch (currentStep) {
      case 1:
        return apiHealthy;
      case 2:
        return wfRunId !== null;
      case 3:
        return Boolean(requestingUserTaskGuid);
      case 4:
        return requestingTaskSubmitted || (requestedItem.trim().length > 0 && justification.trim().length > 0);
      case 5:
        return Boolean(financeUserTaskGuid);
      case 6:
        return financeAssigned;
      case 7:
        return !showResultModal && !workflowCompleted;
      default:
        return false;
    }
  }, [currentStep, apiHealthy, wfRunId, requestingUserTaskGuid, requestedItem, justification, 
      requestingTaskSubmitted,financeUserTaskGuid, financeAssigned, showResultModal, workflowCompleted]);

  // Update status text when step changes
  useEffect(() => {
    const defaultStatuses: Record<number, string> = {
      2: 'Please enter a valid User ID.',
      4: 'Please fill in all required fields.',
      6: 'Please enter a valid User ID.',
      7: 'Please choose approve or decline.',
    };
    
    if (defaultStatuses[currentStep]) {
      setStatus(defaultStatuses[currentStep]);
    }
  }, [currentStep, setStatus]);

  // Focus management for accessibility (keybord control)
  useEffect(() => {
    if (headingRef.current) {
      headingRef.current.setAttribute('tabindex', '-1');
      headingRef.current.focus();
      setTimeout(() => headingRef.current?.removeAttribute('tabindex'), 100);
    }
  }, [currentStep]);

  const handleContinue = () => {
    if (currentStep === 7) {
      completeFinanceTask();
    } else {
      nextStep();
    }
  };

  const { title, description } = stepInfo[currentStep as keyof typeof stepInfo];

  return (
    <>
      <section className="flex flex-col" aria-labelledby='step-heading' aria-busy={isLoading}>
        <Card>
          <CardHeader>
            <CardTitle ref={headingRef} id="step-heading">
              {title}
            </CardTitle>
            <CardDescription>{description}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <StepRenderer step={currentStep} />
              {/* Continue button (not shown for step 4 which has its own) */}
              {currentStep !== 4 && (
                <Button
                  onClick={handleContinue}
                  disabled={!canContinue}
                  className="w-full sm:w-auto"
                >
                  Continue
                </Button>
              )}
            {currentStep === 7 && workflowCompleted && !showResultModal && (
              <RestartControls />
            )}
          </CardContent>
        </Card>
      </section>

      <section className="flex flex-col min-h-0 max-h-full" aria-labelledby="status-heading">
        <Card className="mb-6">
          <CardHeader className="mb-3">
            <CardTitle>Status</CardTitle>
          </CardHeader>
          <CardContent>
            <div 
              className="bg-muted rounded-md p-4 text-sm"
              role="status"
              aria-live="polite"
              aria-atomic="true"
            >
              <p className="whitespace-pre-wrap">{statusText}</p>
            </div>
          </CardContent>
        </Card>

        <Card className="flex-1">
          <CardHeader>
            <CardTitle>API Response</CardTitle>
          </CardHeader>
          <CardContent>
            <div 
              className="overflow-auto max-h-[400px] border-white/20 border"
              role="region"
              aria-live="polite"
              aria-atomic="true"
              aria-labelledby="api-response-heading"
            >
              <SyntaxHighlighter
                language="json"
                style={darcula}
                customStyle={{ 
                  margin: 0, 
                  background: '#0b0b0b', 
                  fontSize: 12,
                }}
                wrapLongLines={true}
                showLineNumbers={false}
              >
                {responseText || '"Response will appear here"'}
              </SyntaxHighlighter>
            </div>
          </CardContent>
        </Card>
      </section>

      {isLoading && (
        <div 
          className="fixed inset-0 bg-black/35 flex items-center justify-center z-50"
          aria-busy="true"
          aria-live="polite"
          aria-label="Loading"
        >
          <Loader2 className="h-10 w-10 animate-spin text-white" />
        </div>
      )}

      <ResultModal />
    </>
  );
};