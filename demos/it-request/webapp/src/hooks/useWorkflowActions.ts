import { useCallback } from 'react';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';

import { UserTaskRunStatus, VariableValue } from 'littlehorse-client/proto';
import type { UserTaskRunId, UserTaskRunIdList } from 'littlehorse-client/proto';
import type { TaskIdRef } from '@/lib/types';
import { createVariableValue } from '@/lib/utils';

import { startItRequest } from '@/app/actions/startItRequest';
import { listUserTasks } from '@/app/actions/listUserTasks';
import { assignUserTask } from '@/app/actions/assignUserTask';
import { completeUserTask } from '@/app/actions/completeUserTask';

export const useWorkflowActions = () => {
  const {
    wfRunId, 
    setLoading, 
    setStatus, 
    setResponse,
  } = useWorkflowContext();

  const extractTaskIds = useCallback((value: UserTaskRunIdList): TaskIdRef[] => {
    return value.results.map((item: UserTaskRunId) => ({
      wfRunId: item.wfRunId!,
      userTaskGuid: item.userTaskGuid
    }));
  }, []);

  const runWorkflow = useCallback(async (userId: string): Promise<string | null> => {
    setLoading(true);
    setStatus('Starting workflow...');
    setResponse('');
    
    try {
      const response = await startItRequest(userId);

      setResponse(JSON.stringify(response, null, 2));
      setStatus('Workflow started successfully');

      return response.id?.id ?? null;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Failed to start workflow');

      return null;
    } finally {
      setLoading(false);
    }
  }, [setLoading, setStatus, setResponse]);

  const findRequestingTask = useCallback(async (userId: string) => {
    if (!wfRunId) return null;
    
    setLoading(true);
    setStatus('Finding requesting task...');
    setResponse('');

    try {
      const queryParams = { userId: userId, status: UserTaskRunStatus.ASSIGNED };
      const data = await listUserTasks(queryParams);

      setResponse(JSON.stringify(data, null, 2));
      
      const assignedCandidates = extractTaskIds(data);
      const assignedMatch = assignedCandidates.find((t) => t.wfRunId.id === wfRunId);

      if (assignedMatch) {
        setStatus(`Found requesting user task assigned to "${userId}".`);
        return assignedMatch.userTaskGuid;
      }

      // Fallback: search by def name
      setStatus('No assigned task found. Searching by definition name...');
      const alt = await listUserTasks({ userTaskDefName: 'it-request' });
      setResponse(JSON.stringify(alt, null, 2));

      const candidates = extractTaskIds(alt);
      const match = candidates.find((t) => t.wfRunId.id === wfRunId);

      if (!match) {
        setStatus('No requesting user task found for this wfRunId.');
        return null;
      }

      setStatus(`Found unassigned requesting task. Assigning to "${userId}"...`);

      try {
        const result = await assignUserTask(match.wfRunId, match.userTaskGuid, { 
          userId: userId, 
          overrideClaim: true 
        });
        
        setResponse(JSON.stringify(result, null, 2));
      } catch (err) {
        
        setResponse(JSON.stringify({ error: err }, null, 2));
      }
      
      setStatus(`Assigned requesting task to "${userId} (found by def)".`);

      return match.userTaskGuid;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus(`Failed to find/assign requesting user task: ${message}`);
      
      return null;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, extractTaskIds, setLoading, setStatus, setResponse]);

  const completeRequestingTask = useCallback(async (
    userId: string,
    taskGuid: string,
    requestedItem: string,
    justification: string
  ): Promise<boolean> => {
    setLoading(true);
    setStatus('Completing requesting task...');
    setResponse('');

    try {
      const results: Record<string, VariableValue> = {
        requestedItem: createVariableValue('STR', requestedItem),
        justification: createVariableValue('STR', justification)
      };

      if (!wfRunId) {
        throw new Error('wfRunId is required');
      }

      const response = await completeUserTask({ id: wfRunId }, taskGuid, userId, results);

      setResponse(JSON.stringify(response, null, 2));
      setStatus('Request submitted successfully');

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Failed to submit request');

      return false;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, setLoading, setStatus, setResponse]);

  const findFinanceTask = useCallback(async () => {
    if (!wfRunId) return null;

    setLoading(true);
    setStatus('Finding Finance task... Looking for a pending Finance review task.');
    setResponse('');

    try {
      const data = await listUserTasks({ userGroup: 'finance' });
      setResponse(JSON.stringify(data, null, 2));

      const candidates = extractTaskIds(data);
      const match = candidates.find((t) => t.wfRunId.id === wfRunId);
      
      if (match) {
        setStatus(`Found Finance task awaiting action.`);
        return match.userTaskGuid;
      } else {
        setStatus('No Finance task found at this time.');
        return null;
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus(`Failed to find Finance task: ${message}`);

      return null;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, extractTaskIds, setLoading, setStatus, setResponse]); 

  const assignFinanceTask = useCallback(async (
    userId: string,
    overrideClaim: boolean,
    financeTaskGuid: string
  ): Promise<boolean> => {
    if (!financeTaskGuid || !wfRunId) return false;

    setLoading(true);
    setStatus('Assigning task...');
    setResponse('');

    try {
      setLoading(true);
      setStatus('Assigning Finance task...');
      setResponse('');
      
      const response =await assignUserTask(
        { id: wfRunId },
        financeTaskGuid,
        { userId, overrideClaim }
      );
      
      setResponse(JSON.stringify(response, null, 2));

      setStatus(`Task successfully assigned to ${userId}`);

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Error: Failed to assign task');

      return false;
    } finally {
      setLoading(false);
    }
  }, [ wfRunId, setLoading, setStatus, setResponse]);

  const completeFinanceTask = useCallback(async (
    decision: 'APPROVE' | 'DECLINE',
    financeTaskGuid: string,
    userId: string
  ) => {
    if (!financeTaskGuid || !wfRunId) return false;

    setLoading(true);
    setStatus('Submitting decision...');
    setResponse('');

    try {
      const results: Record<string, VariableValue> = {
        isApproved: createVariableValue('BOOL', decision === 'APPROVE')
      };

      const response = await completeUserTask({ id: wfRunId }, financeTaskGuid, userId, results);
      
      setResponse(JSON.stringify(response, null, 2));
      setStatus('Decision submitted successfully');

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Error: Failed to submit decision');

      return false;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, setLoading, setStatus, setResponse]);

  return {
    runWorkflow,
    findRequestingTask,
    completeRequestingTask,
    findFinanceTask,
    assignFinanceTask,
    completeFinanceTask
  };
};