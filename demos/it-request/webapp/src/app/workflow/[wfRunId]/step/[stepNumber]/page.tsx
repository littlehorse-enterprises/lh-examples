import { redirect } from 'next/navigation';
import Image from 'next/image';

import { WorkflowProvider } from '@/components/providers/WorkflowProvider';
import { StepContainer } from '@/components/steps/StepContainer';
import { StatusPanel } from '@/components/StatusPanel';
import { ResponsePanel } from '@/components/ResponsePanel';

interface PageProps {
  params: Promise<{
    wfRunId: string;
    stepNumber: string;
  }>;
  searchParams: Promise<{
    taskGuid?: string;
    userId?: string;
  }>;
}

export default async function StepPage({ params, searchParams }: PageProps) {
  const { wfRunId, stepNumber } = await params;
  const { taskGuid, userId } = await searchParams;

  const step = parseInt(stepNumber);
  const isNewWorkflow = wfRunId === 'new';
  
  // Validate step number
  if (isNaN(step) || step < 1 || step > 7) {
    redirect('/workflow/new/step/1');
  }

  // Don't allow steps > 2 without a wfRunId
  if (isNewWorkflow && step > 2) {
    redirect('/workflow/new/step/1');
  }

  return (
    <WorkflowProvider 
      wfRunId={isNewWorkflow ? null : wfRunId}
      currentStep={step}
      userId={userId}
      taskGuid={taskGuid}
    >
      <>
        {/* Header */}
        <header id="header" role="banner" className="h-16 border-b border-white/20">
          <div className="flex items-center justify-start mx-auto max-w-5xl py-4 px-6">
            <Image 
              src="/logo.png" 
              alt="LittleHorse" 
              width={200}
              height={60}
              className="block"
            />
            <h1 className="text-2xl font-normal m-0 ml-auto">User Tasks / IT Request Example</h1>
          </div>
        </header>
        
        {/* Main App */}
        <main id="main" role="main" className="min-h-[calc(100vh-4rem)] flex flex-col mx-auto max-w-5xl py-6 px-6">
          <p className="border-b border-white/20 leading-relaxed mb-6 opacity-90 pb-6">
            This UI guides you through the LittleHorse User Tasks IT Request example. A requester starts an
            IT Request workflow, completes a requesting task by providing the Requested Item and a Justification,
            then a Finance user reviews and either approves or declines. The app validates user input before calling
            the API and always shows the latest response for the current step.
          </p>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 flex-1 min-h-0 items-baseline">
            <StepContainer />
            <section className="flex flex-col min-h-0 max-h-full">
              <StatusPanel />
              <ResponsePanel />
            </section>
          </div>
        </main>
      </>
    </WorkflowProvider>
  );
}