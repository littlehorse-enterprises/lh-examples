import { useEffect, useMemo, useRef, useState } from 'react';
import type React from 'react';
import type { ChangeEvent, MouseEvent } from 'react';
import {
  assignUserTask,
  completeUserTask,
  listUserTasks,
  startItRequest,
  health,
  type TaskSearchResult,
  type TaskIdRef,
  type UserTaskFieldValue,
    type RunWfResponse,
    deleteWfRun,
    deleteAllWfRunsForSpec
} from './api';
import './index.css';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { darcula } from 'react-syntax-highlighter/dist/esm/styles/prism';

type StepNumber = 1 | 2 | 3 | 4 | 5 | 6 | 7;

type FinanceDecision = 'APPROVE' | 'DECLINE';

function isValidUserId(userId: string): boolean {
  return userId.trim().length >= 3;
}

export default function App(): React.ReactElement {
  // Global UI state
  const [currentStep, setCurrentStep] = useState<StepNumber>(1);
  const [statusText, setStatusText] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [responseText, setResponseText] = useState<string>('');
  const [apiHealthy, setApiHealthy] = useState<boolean>(false);

  // Shared workflow state captured across steps
  const [requestingUserId, setRequestingUserId] = useState<string>('');
  const [wfRunId, setWfRunId] = useState<string>('');
  const [requestingUserTaskGuid, setRequestingUserTaskGuid] = useState<string>('');
  const [financeUserTaskGuid, setFinanceUserTaskGuid] = useState<string>('');

  // Step 4 inputs
  const [requestedItem, setRequestedItem] = useState<string>('');
  const [justification, setJustification] = useState<string>('');
  const [requestingTaskSubmitted, setRequestingTaskSubmitted] = useState<boolean>(false);

  // Step 6 inputs
  const [financeAssigneeUserId, setFinanceAssigneeUserId] = useState<string>('');
  const [financeOverride, setFinanceOverride] = useState<boolean>(false);
  const [financeAssigned, setFinanceAssigned] = useState<boolean>(false);

  // Step 7 inputs
  const [financeDecision, setFinanceDecision] = useState<FinanceDecision>('APPROVE');
  const [showResultModal, setShowResultModal] = useState<boolean>(false);
  const [workflowCompleted, setWorkflowCompleted] = useState<boolean>(false);
  const [deleteScope, setDeleteScope] = useState<'none' | 'current' | 'all'>('none');

  // Derived enables for Continue button depending on step rules
  const canContinue: boolean = useMemo<boolean>(() => {
    switch (currentStep) {
      case 1:
        return apiHealthy;
      case 2:
        return Boolean(wfRunId);
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
  }, [currentStep, wfRunId, statusText, requestingUserTaskGuid, requestedItem, justification, requestingTaskSubmitted, financeUserTaskGuid, financeAssigned, showResultModal, workflowCompleted]);

  // Modal focus trap
  const modalRef = useRef<HTMLDivElement | null>(null);
  const headerRef = useRef<HTMLDivElement | null>(null);
  const appRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!showResultModal) return;    
    const container = modalRef.current;
    if (!container) return;

    const focusableSelectors = [
      'a[href]', 'button:not([disabled])', 'textarea:not([disabled])', 'input:not([disabled])', 'select:not([disabled])', '[tabindex]:not([tabindex="-1"])'
    ].join(',');

    const focusable = Array.from(container.querySelectorAll<HTMLElement>(focusableSelectors));
    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (first) first.focus();

    function handleKeyDown(e: KeyboardEvent): void {
      if (e.key === 'Escape') {
        e.preventDefault();
        setShowResultModal(false);
        return;
      }

      if (e.key !== 'Tab' || focusable.length === 0) return;

      if (e.shiftKey) {
        if (document.activeElement === first) {
          e.preventDefault();
          last?.focus();
        }
      } else {
        if (document.activeElement === last) {
          e.preventDefault();
          first?.focus();
        }
      }
    }

    document.addEventListener('keydown', handleKeyDown);

    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [showResultModal]);

  // When modal opens, mark background as inert and blur any focused element within it.
  useEffect(() => {    
    const containers: Array<HTMLElement | null> = [headerRef.current, appRef.current];
    
    containers.forEach((el) => {
      if (!el) return;
      if (showResultModal) el.setAttribute('inert', ''); else el.removeAttribute('inert');
    });

    if (showResultModal) {
      const active = document.activeElement as HTMLElement | null;

      if (active && containers.some((el) => el && el.contains(active))) {
        active.blur();
      }
    } else {
      // Focus the first interactive element in the current step
      requestAnimationFrame(() => {
        const firstFocusable = document.querySelector(
          'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
        ) as HTMLElement;
        if (firstFocusable) {
          firstFocusable.focus();
        }
      });
    }
  }, [showResultModal]);

  function goToStep(next: StepNumber): void {
    setCurrentStep(next);
    setResponseText('');
    // Move focus to the main heading for screen readers when step changes
    requestAnimationFrame(() => {
      const el = document.getElementById('step-heading');
      if (el) el.focus();
    });
  }

  // Effect for step 1: automatically check health on mount and whenever we land on step 1
  useEffect(() => {
    if (currentStep !== 1) return;

    setStatusText('Checking API health… Ensuring the backend is reachable.');
    setIsLoading(true);
    setApiHealthy(false);

    setResponseText('');

    health()
      .then((data: { ok: boolean }) => {
        setResponseText(JSON.stringify(data, null, 2));
        setStatusText(data.ok ? 'API health check passed. Service is reachable.' : 'API health check failed. Service is not reachable.');
        setApiHealthy(Boolean(data.ok));
      })
      .catch((err: unknown) => {
        const message: string = err instanceof Error ? err.message : 'Unknown error';
        setResponseText(JSON.stringify({ error: message }, null, 2));
        setStatusText(`API health check failed: ${message}`);
        setApiHealthy(false);
      })
      .finally(() => setIsLoading(false));
  }, [currentStep]);

  // Effect for step 3: auto fetch requesting user tasks; if none found, try by def name and assign to user
  useEffect(() => {
    if (currentStep !== 3) return;

    setStatusText('Finding requesting user task… Looking for a task currently assigned to you.');
    setIsLoading(true);
    setResponseText('');

    const queryParams: { userId: string; status: string } = { userId: requestingUserId, status: 'ASSIGNED' };

    listUserTasks(queryParams)
      .then(async (data: TaskSearchResult) => {
        setResponseText(JSON.stringify(data, null, 2));
        
        // Prefer the task matching the wfRunId we just started
        const assignedCandidates: TaskIdRef[] = extractTaskIds(data);
        const assignedMatch: TaskIdRef | undefined = assignedCandidates.find((t) => t.wfRunId === wfRunId);

        if (assignedMatch) {
          setRequestingUserTaskGuid(assignedMatch.userTaskGuid);
          setStatusText(`Found requesting user task assigned to "${requestingUserId}". wfRunId=${assignedMatch.wfRunId}, userTaskGuid=${assignedMatch.userTaskGuid}`);
          return;
        }

        // Fallback: task may have been released; search by def name and still match wfRunId
        setStatusText('No assigned task found. Searching by definition name "it-request" for released tasks…');
        const alt = await listUserTasks({ userTaskDefName: 'it-request' });
        setResponseText(JSON.stringify(alt, null, 2));
        const candidates: TaskIdRef[] = extractTaskIds(alt);
        const match: TaskIdRef | undefined = candidates.find((t) => t.wfRunId === wfRunId);

        if (!match) {
          setRequestingUserTaskGuid('');
          setStatusText('No requesting user task found for this wfRunId. Please verify the workflow started correctly.');
          return;
        }

        setStatusText(`Found unassigned requesting task. Assigning to "${requestingUserId}"…`);
        await assignUserTask(match.wfRunId, match.userTaskGuid, { userId: requestingUserId, override: true });
        setRequestingUserTaskGuid(match.userTaskGuid);
        setStatusText(`Assigned requesting task to "${requestingUserId}". wfRunId=${match.wfRunId}, userTaskGuid=${match.userTaskGuid}`);
      })
      .catch((err: unknown) => {
        const message: string = err instanceof Error ? err.message : 'Unknown error';
        setResponseText(JSON.stringify({ error: message }, null, 2));
        setRequestingUserTaskGuid('');
        setStatusText(`Failed to find/assign requesting user task: ${message}`);
      })
      .finally(() => setIsLoading(false));
  }, [currentStep, requestingUserId]);

  // Effect for step 5: auto fetch finance task
  useEffect(() => {
    if (currentStep !== 5) return;

    setStatusText('Finding Finance task… Looking for a pending Finance review task.');
    setIsLoading(true);
    setResponseText('');

    listUserTasks({ userGroup: 'finance' })
      .then((data: TaskSearchResult) => {
        setResponseText(JSON.stringify(data, null, 2));
        const candidates: TaskIdRef[] = extractTaskIds(data);
        const match: TaskIdRef | undefined = candidates.find((t) => t.wfRunId === wfRunId);
        if (match) {
          setFinanceUserTaskGuid(match.userTaskGuid);
          setStatusText(`Found Finance task awaiting action. wfRunId=${match.wfRunId}, userTaskGuid=${match.userTaskGuid}`);
        } else {
          setFinanceUserTaskGuid('');
          setStatusText('No Finance task found at this time. Try again shortly if the workflow is still progressing.');
        }
      })
      .catch((err: unknown) => {
        const message: string = err instanceof Error ? err.message : 'Unknown error';
        setResponseText(JSON.stringify({ error: message }, null, 2));
        setFinanceUserTaskGuid('');
        setStatusText(`Failed to find Finance task: ${message}`);
      })
      .finally(() => setIsLoading(false));
  }, [currentStep]);

  async function handleRunItWorkflow(e: MouseEvent<HTMLButtonElement>): Promise<void> {
    e.preventDefault();

    setStatusText('Running workflow...');
    setIsLoading(true);
    setResponseText('');

    try {
      const data: RunWfResponse = await startItRequest(requestingUserId);
      setResponseText(JSON.stringify(data, null, 2));
      const idValue: string | undefined = data?.id?.id;
      setWfRunId(idValue ?? '');

      if (idValue) {
        setStatusText('OK');
      } else {
        setStatusText('Error — wfRunId missing');
      }
    } catch (err: unknown) {
      const message: string = err instanceof Error ? err.message : 'Unknown error';
      setResponseText(JSON.stringify({ error: message }, null, 2));
      setStatusText('Error');
      setWfRunId('');
    } finally {
      setIsLoading(false);
    }
  }

  async function handleCompleteRequestingTask(): Promise<void> {
    setIsLoading(true);
    setResponseText('');

    try {
      const results: Record<string, UserTaskFieldValue> = {
        requestedItem,
        justification
      };

      await completeUserTask(wfRunId, requestingUserTaskGuid, requestingUserId, results);

      setResponseText(JSON.stringify({ ok: true }, null, 2));
      setStatusText('OK');
      setRequestingTaskSubmitted(true);
    } catch (err: unknown) {
      const message: string = err instanceof Error ? err.message : 'Unknown error';

      setResponseText(JSON.stringify({ error: message }, null, 2));
      setStatusText('Error');
    } finally {
      setIsLoading(false);
    }
  }

  async function handleAssignFinanceTask(e: MouseEvent<HTMLButtonElement>): Promise<void> {
    e.preventDefault();

    setStatusText('Assigning task...');
    setIsLoading(true);
    setResponseText('');

    try {
      await assignUserTask(wfRunId, financeUserTaskGuid, { userId: financeAssigneeUserId, override: financeOverride });
      
      setResponseText(JSON.stringify({ ok: true, wfRunId, userId: financeAssigneeUserId, userTaskGuid: financeUserTaskGuid }, null, 2));
      setStatusText(`OK — wfRunId=${wfRunId}, userId=${financeAssigneeUserId}, userTaskGuid=${financeUserTaskGuid}`);
      setFinanceAssigned(true);
    } catch (err: unknown) {
      const message: string = err instanceof Error ? err.message : 'Unknown error';
      
      setResponseText(JSON.stringify({ error: message }, null, 2));
      setStatusText('Error');
      setFinanceAssigned(false);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleCompleteFinanceTask(): Promise<void> {
    setIsLoading(true);
    setResponseText('');

    try {
      const results: Record<string, UserTaskFieldValue> = {
        isApproved: financeDecision === 'APPROVE'
      };

      await completeUserTask(wfRunId, financeUserTaskGuid, financeAssigneeUserId, results);

      setResponseText(JSON.stringify({ ok: true }, null, 2));
      setStatusText('OK');

      // Blur any currently focused background control before opening the modal
      const active = document.activeElement as HTMLElement | null;
      if (active) active.blur();

      setWorkflowCompleted(true);
      setShowResultModal(true);
    } catch (err: unknown) {
      const message: string = err instanceof Error ? err.message : 'Unknown error';

      setResponseText(JSON.stringify({ error: message }, null, 2));
      setStatusText('Error');
    } finally {
      setIsLoading(false);
    }
  }

  async function handleRestart(): Promise<void> {
    setIsLoading(true);
    setResponseText('');
    try {
      // Perform deletions based on scope selection
      if (deleteScope === 'current' && wfRunId) {
        await deleteWfRun(wfRunId);
        setStatusText('Deleted current wfRun.');
      } else if (deleteScope === 'all') {
        // This example's spec name is fixed
        const result = await deleteAllWfRunsForSpec('it-request');
        setStatusText(`Deleted ${result.deleted} wfRun(s) for spec "it-request".`);
      }

      // Reset all app state
      setCurrentStep(1);
      setRequestingUserId('');
      setWfRunId('');
      setRequestingUserTaskGuid('');
      setFinanceUserTaskGuid('');
      setRequestedItem('');
      setJustification('');
      setRequestingTaskSubmitted(false);
      setFinanceAssigneeUserId('');
      setFinanceOverride(false);
      setFinanceAssigned(false);
      setFinanceDecision('APPROVE');
      setShowResultModal(false);
      setWorkflowCompleted(false);
      setDeleteScope('none');
      setResponseText('');
      setStatusText('Restarted.');
    } catch (err: unknown) {
      const message: string = err instanceof Error ? err.message : 'Unknown error';
      setResponseText(JSON.stringify({ error: message }, null, 2));
      setStatusText(`Failed to restart: ${message}`);
    } finally {
      setIsLoading(false);
    }
  }

  function handleContinue(): void {
    const next: StepNumber = (currentStep + 1) as StepNumber;
    if (currentStep < 7) {
      goToStep(next);
    }
  }

  // Heading text per step
  const heading: string = useMemo<string>(() => {
    switch (currentStep) {
      case 1: return 'Step 1: Check API health';
      case 2: return 'Step 2: Run IT Request workflow';
      case 3: return 'Step 3: Find requesting user tasks';
      case 4: return 'Step 4: Complete requesting user task';
      case 5: return 'Step 5: Find the Finance task';
      case 6: return 'Step 6: Assign Finance task to a user';
      case 7: return 'Step 7: Complete Finance task';
      default: return '';
    }
  }, [currentStep]);

  // Status text defaults per step when idle
  useEffect(() => {
    if (currentStep === 2) setStatusText('Please enter your User ID.');
    if (currentStep === 4) setStatusText('Please fill in all required fields.');
    if (currentStep === 6) setStatusText('Please enter a User ID.');
    if (currentStep === 7) setStatusText('Please choose approve or decline.');
    if (currentStep === 4) setRequestingTaskSubmitted(false);
  }, [currentStep]);

  // Helpers to robustly extract task ids from server response
  function isTaskIdRefLike(value: unknown): value is TaskIdRef {
    if (typeof value !== 'object' || value === null) return false;

    const obj = value as Record<string, unknown>;
    if (typeof obj.wfRunId === 'string' && typeof obj.userTaskGuid === 'string') return true;

    // Support shape: { wfRunId: { id: string }, userTaskGuid: string }
    const wfObj = obj.wfRunId as unknown;

    return (
      typeof obj.userTaskGuid === 'string' &&
      typeof wfObj === 'object' && wfObj !== null && typeof (wfObj as Record<string, unknown>).id === 'string'
    );
  }

  function extractTaskIds(value: unknown): TaskIdRef[] {
    // Case 1: expected shape { ids: { ids: TaskIdRef[] } }
    if (
      typeof value === 'object' && value !== null &&
      'ids' in (value as Record<string, unknown>) &&
      typeof (value as Record<string, unknown>).ids === 'object' && (value as Record<string, any>).ids !== null &&
      Array.isArray((value as Record<string, any>).ids.ids)
    ) {
      const arr: unknown[] = (value as Record<string, any>).ids.ids;
      return arr
        .filter(isTaskIdRefLike)
        .map((obj: any) => ({
          wfRunId: typeof obj.wfRunId === 'string' ? obj.wfRunId : obj.wfRunId?.id,
          userTaskGuid: obj.userTaskGuid,
        }));
    }

    // Case 2: { ids: TaskIdRef[] }
    if (
      typeof value === 'object' && value !== null &&
      Array.isArray((value as Record<string, any>).ids)
    ) {
      const arr: unknown[] = (value as Record<string, any>).ids;
      return arr
        .filter(isTaskIdRefLike)
        .map((obj: any) => ({
          wfRunId: typeof obj.wfRunId === 'string' ? obj.wfRunId : obj.wfRunId?.id,
          userTaskGuid: obj.userTaskGuid,
        }));
    }

    // Case 3: { results: Array<{ wfRunId: { id: string }, userTaskGuid: string }> }
    if (
      typeof value === 'object' && value !== null &&
      Array.isArray((value as Record<string, any>).results)
    ) {
      const arr: unknown[] = (value as Record<string, any>).results;
      return arr
        .filter(isTaskIdRefLike)
        .map((item: unknown) => {
          const obj = item as any;
          const wfRunIdValue = typeof obj.wfRunId === 'string' ? obj.wfRunId : obj.wfRunId?.id;
          return { wfRunId: wfRunIdValue as string, userTaskGuid: obj.userTaskGuid as string } as TaskIdRef;
        });
    }

    // Case 4: TaskIdRef[]
    if (Array.isArray(value)) {
      return (value as unknown[])
        .filter(isTaskIdRefLike)
        .map((obj: any) => ({
          wfRunId: typeof obj.wfRunId === 'string' ? obj.wfRunId : obj.wfRunId?.id,
          userTaskGuid: obj.userTaskGuid,
        }));
    }

    return [];
  }

  return (
    <div className="page">
      <div ref={headerRef} className="app-header" aria-hidden={showResultModal}>
        <div className="app-header__inner">
          <img className="app-header__logo" src="/logo.png" alt="LittleHorse" />
          <h1 className="app-header__title">User Tasks / IT Request Example</h1>
        </div>
      </div>
      <div ref={appRef} className="app" aria-hidden={showResultModal}>
      <div className="app-intro">
        <p className="app-intro__text">
          This UI guides you through the LittleHorse User Tasks IT Request example. A requester starts an
          IT Request workflow, completes a requesting task by providing the Requested Item and a Justification,
          then a Finance user reviews and either approves or declines. The app validates user input before calling
          the API and always shows the latest response for the current step.
        </p>
      </div>

      <main id="main" className="app__grid" role="main" aria-busy={isLoading}>
        {/* Left column: controls for the current step */}
        <section className="app__left" aria-labelledby="step-heading">
          <h2 id="step-heading" tabIndex={-1} className="app__step-title">{heading}</h2>
          {/* Step-specific short instructions */}
          <p className="app__step-hint">
            {((): string => {
              switch (currentStep) {
                case 1:
                  return 'We automatically check the API health. Wait for the result; Continue unlocks if healthy.';
                case 2:
                  return 'Enter a valid User ID (min 3 chars) and start the IT Request workflow. On success we keep the wfRunId.';
                case 3:
                  return 'We locate your requesting task. If your claim expired, we find it by definition and assign it back to you.';
                case 4:
                  return 'Provide the Requested Item and Justification (both required), then submit to complete the requesting task.';
                case 5:
                  return 'We look for the Finance review task created by the workflow.';
                case 6:
                  return 'Enter the Finance User ID to assign the task. Optionally enable override to take the task if needed.';
                case 7:
                  return 'Choose to approve or decline the IT request, then submit the decision to finish.';
                default:
                  return '';
              }
            })()}
          </p>
          
          {currentStep === 2 && (
            <div>
              <label className="form__field" htmlFor="requester-id">
                <span className="form__label">User ID</span>
                <input
                  className="form__input"
                  type="text"
                  id="requester-id"
                  required
                  aria-required={true}
                  aria-invalid={!isValidUserId(requestingUserId) && requestingUserId.trim().length > 0}
                  value={requestingUserId}
                  onChange={(e: ChangeEvent<HTMLInputElement>): void => setRequestingUserId(e.target.value)}
                  placeholder="Enter User ID"
                  disabled={Boolean(wfRunId)}
                />
              </label>
              <div className="app__row">
                <button
                  className="app__button"
                  onClick={(e): void => { if (isValidUserId(requestingUserId)) void handleRunItWorkflow(e); }}
                  disabled={!isValidUserId(requestingUserId) || Boolean(wfRunId)}
                >
                  Run IT workflow
                </button>
              </div>
            </div>
          )}

          {currentStep === 3 && (
            <div>
              {wfRunId && <p className="app__text"><strong>wfRunId:</strong> {wfRunId}</p>}
              {requestingUserTaskGuid && <p className="app__text"><strong>userTaskGuid:</strong> {requestingUserTaskGuid}</p>}
            </div>
          )}

          {currentStep === 4 && (
            <div>
              <label className="form__field" htmlFor="requested-item">
                <span className="form__label">Requested Item</span>
                <input
                  className="form__input"
                  type="text"
                  id="requested-item"
                  required
                  aria-required={true}
                  aria-invalid={requestedItem.trim().length === 0}
                  value={requestedItem}
                  onChange={(e: ChangeEvent<HTMLInputElement>): void => setRequestedItem(e.target.value)}
                  placeholder="What do you need?"
                  disabled={requestingTaskSubmitted}
                />
              </label>
              <label className="form__field" htmlFor="justification">
                <span className="form__label">Justification</span>
                <input
                  className="form__input"
                  type="text"
                  id="justification"
                  required
                  aria-required={true}
                  aria-invalid={justification.trim().length === 0}
                  value={justification}
                  onChange={(e: ChangeEvent<HTMLInputElement>): void => setJustification(e.target.value)}
                  placeholder="Why is it needed?"
                  disabled={requestingTaskSubmitted}
                />
              </label>
              <div className="app__row">
                {!requestingTaskSubmitted ? (
                  <button className="app__button" onClick={(): void => { void handleCompleteRequestingTask(); }} disabled={!(requestedItem.trim().length > 0 && justification.trim().length > 0)}>
                    Continue
                  </button>
                ) : (
                  <button className="app__button" onClick={handleContinue}>
                    Continue
                  </button>
                )}
              </div>
            </div>
          )}

          {currentStep === 5 && (
            <div>
              {financeUserTaskGuid && <p className="app__text"><strong>Finance userTaskGuid:</strong> {financeUserTaskGuid}</p>}
            </div>
          )}

          {currentStep === 6 && (
            <div>
              <label className="form__field" htmlFor="finance-user-id">
                <span className="form__label">User ID</span>
                <input
                  className="form__input"
                  type="text"
                  id="finance-user-id"
                  required
                  aria-required={true}
                  aria-invalid={!isValidUserId(financeAssigneeUserId) && financeAssigneeUserId.trim().length > 0}
                  value={financeAssigneeUserId}
                  onChange={(e: ChangeEvent<HTMLInputElement>): void => setFinanceAssigneeUserId(e.target.value)}
                  placeholder="Enter User ID"
                  disabled={financeAssigned}
                />
              </label>
              <label className="form__field app__row app__row--align-center" htmlFor="finance-override">
                <input
                  className="form__checkbox"
                  type="checkbox"
                  id="finance-override"
                  checked={financeOverride}
                  onChange={(e: ChangeEvent<HTMLInputElement>): void => setFinanceOverride(e.target.checked)}
                  disabled={financeAssigned}
                />
                <span>Enable override</span>
              </label>
              <div className="app__row">
                <button
                  className="app__button"
                  onClick={(e): void => { if (isValidUserId(financeAssigneeUserId)) void handleAssignFinanceTask(e); }}
                  disabled={!isValidUserId(financeAssigneeUserId) || financeAssigned}
                >
                  Assign task
                </button>
              </div>
            </div>
          )}

          {currentStep === 7 && (
            <div>
              <label className="form__field app__row app__row--align-center" htmlFor="approve-checkbox">
                <input
                  className="form__checkbox"
                  type="checkbox"
                  id="approve-checkbox"
                  checked={financeDecision === 'APPROVE'}
                  onChange={(e: ChangeEvent<HTMLInputElement>): void => setFinanceDecision(e.target.checked ? 'APPROVE' : 'DECLINE')}
                  disabled={showResultModal || workflowCompleted}
                />
                <span>Approve IT request</span>
              </label>
            </div>
          )}

          {/* Default Continue button, disabled unless canContinue */}
          {currentStep !== 4 && (
            <div className="app__row mt-16">
              <button className="app__button" onClick={(): void => {
                if (currentStep === 7) {
                  void handleCompleteFinanceTask();
                } else {
                  handleContinue();
                }
              }} disabled={!canContinue}>
                Continue
              </button>
            </div>
          )}

          {/* Step 7 post-completion controls: Restart + delete options */}
          {currentStep === 7 && workflowCompleted && !showResultModal && (
            <div className="mt-16">
              <div className="app__row">
                <button
                  className="app__button"
                  onClick={(): void => { void handleRestart(); }}
                >
                  Restart
                </button>
              </div>
              <div className="spacer-12" />
              <fieldset className="form__field" aria-label="Delete options">
                <legend className="form__label">Delete options</legend>
                <label className="form__field app__row app__row--align-center" htmlFor="delete-current">
                  <input
                    className="form__checkbox"
                    type="checkbox"
                    id="delete-current"
                    checked={deleteScope === 'current'}
                    onChange={(e: ChangeEvent<HTMLInputElement>): void => setDeleteScope(e.target.checked ? 'current' : 'none')}
                  />
                  <span>Delete current wfRun</span>
                </label>
                <label className="form__field app__row app__row--align-center" htmlFor="delete-all">
                  <input
                    className="form__checkbox"
                    type="checkbox"
                    id="delete-all"
                    checked={deleteScope === 'all'}
                    onChange={(e: ChangeEvent<HTMLInputElement>): void => setDeleteScope(e.target.checked ? 'all' : 'none')}
                  />
                  <span>Delete every wfRun</span>
                </label>
              </fieldset>
            </div>
          )}
        </section>

          {/* Right column: status + response viewer */}
          <section className="app__response" aria-labelledby="status-heading">
            <h3 id="status-heading" className="app__response-title">Status</h3>
            <div className="app__status-viewer" role="status" aria-live="polite" aria-atomic="true">
              <p className="no-margin pre-wrap">{statusText}</p>
            </div>
            <div className="spacer-12" />
            <h3 className="app__response-title" id="api-response-heading">API Response</h3>
            <div className="app__response-viewer" role="region" aria-live="polite" aria-atomic="true" aria-labelledby="api-response-heading">
              <SyntaxHighlighter
                className="code-block"
                language="json"
                style={darcula}
                customStyle={{ margin: 0, background: '#0b0b0b', fontSize: 12 }}
                wrapLongLines={true}
                wrapLines={true}
                lineProps={{ style: { whiteSpace: 'pre-wrap', wordBreak: 'break-word' } }}
                codeTagProps={{ className: 'code-content' }}
                showLineNumbers={false}
              >
                {responseText || '"Response will appear here"'}
              </SyntaxHighlighter>
            </div>
          </section>
      </main>

      {/* Preloader overlay */}
      {isLoading && (
        <div className="app__overlay" aria-busy="true" aria-live="polite" aria-label="Loading">
          <div className="app__spinner" />
        </div>
      )}

      </div>

      {/* Result modal (render outside of .app so it's not affected by inert) */}
      {showResultModal && (
        <div className="app-modal__backdrop" role="dialog" aria-modal="true" aria-labelledby="result-title" aria-describedby="result-desc">
          <div ref={modalRef} className="app-modal__content" role="document">
            <h3 id="result-title" className="no-margin">Result</h3>
            <p id="result-desc" className="emoji-24">{financeDecision === 'APPROVE'
              ? 'The IT request has been approved. An email has been sent to the user.'
              : 'The IT request has been declined. An email has been sent to the user.'
            }</p>
            <div className="app__row center">
              <button autoFocus className="app__button" onClick={(): void => setShowResultModal(false)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}