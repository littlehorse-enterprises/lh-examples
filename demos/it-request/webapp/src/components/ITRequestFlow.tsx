'use client';

import { useMemo, useRef } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { darcula } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { HealthCheckStep } from './steps/HealthCheckStep';

const stepInfo = {
  1: {
    title: 'Step 1: Check API health',
    description: 'API health check: Ensuring the backend is reachable.',
  }
};

const StepRenderer = ({ step }: { step: number }) => {
  switch (step) {
    case 1:
      return <HealthCheckStep />;
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
    isLoading,
    statusText,
    responseText,
    nextStep,
  } = useWorkflowStore();
  
  const canContinue = useMemo(() => {
    switch (currentStep) {
      case 1:
        return apiHealthy;
      default:
        return false;
    }
  }, [currentStep, apiHealthy, wfRunId, requestingUserTaskGuid]);  

  const handleContinue = () => {
   // TODO: add continue logic
   nextStep();
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
            <Button
              onClick={handleContinue}
              disabled={!canContinue}
              className="w-full sm:w-auto"
            >
              Continue
            </Button>
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
    </>
  );
};