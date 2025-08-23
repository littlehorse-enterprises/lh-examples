import { useCallback } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';

import { startItRequest } from '@/app/actions/startItRequest';
import { listUserTasks } from '@/app/actions/listUserTasks';
import { assignUserTask } from '@/app/actions/assignUserTask';
import { completeUserTask } from '@/app/actions/completeUserTask';
import { deleteWfRun } from '@/app/actions/deleteWfRun';
import { deleteAllWfRunsForSpec } from '@/app/actions/deleteAllWfRunsForSpec';

import type { TaskIdRef } from '@/lib/types';
import { UserTaskRunId, UserTaskRunIdList, UserTaskRunStatus, VariableValue } from 'littlehorse-client/proto';
import { createVariableValue } from '@/lib/utils';

export const useWorkflowOperations = () => {
  const {
    requestingUserId,
    wfRunId,
    requestingUserTaskGuid,
    financeUserTaskGuid,
    requestedItem,
    justification,
    financeAssigneeUserId,
    financeOverride,
    financeDecision,
    deleteScope,
    setWfRunId,
    setRequestingUserTaskGuid,
    setFinanceUserTaskGuid,
    setLoading,
    setStatus,
    setResponse,
    setRequestingTaskSubmitted,
    setFinanceAssigned,
    setWorkflowCompleted,
    setShowResultModal,
    reset,
  } = useWorkflowStore();

  const extractTaskIds = useCallback((value: UserTaskRunIdList): TaskIdRef[] => {
    return value.results.map((item: UserTaskRunId) => ({
      wfRunId: item.wfRunId!,
      userTaskGuid: item.userTaskGuid
    }));
  }, []);

  const runWorkflow = useCallback(async () => {
    setLoading(true);
    setStatus('Running workflow...');
    setResponse('');

    try {
      const data = await startItRequest(requestingUserId);
      setResponse(JSON.stringify(data, null, 2));
      const wfRunId = data?.id;
      
      if (wfRunId) {
        setWfRunId(wfRunId);
        setStatus('OK');

        return true;
      } else {
        setStatus('Error — wfRunId missing');

        return false;
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Error');
      setWfRunId(null);

      return false;
    } finally {
      setLoading(false);
    }
  }, [requestingUserId, setLoading, setStatus, setResponse, setWfRunId]);

  const findRequestingTask = useCallback(async () => {
    setLoading(true);
    setStatus(`Finding requesting user task... Looking for a task currently assigned to ${requestingUserId}.`);
    setResponse('');

    try {
      const queryParams = { userId: requestingUserId, status: UserTaskRunStatus.ASSIGNED };
      const data = await listUserTasks(queryParams);

      setResponse(JSON.stringify(data, null, 2));
      
      const assignedCandidates = extractTaskIds(data);
      const assignedMatch = assignedCandidates.find((t) => t.wfRunId.id === wfRunId?.id);

      if (assignedMatch) {
        setRequestingUserTaskGuid(assignedMatch.userTaskGuid);
        setStatus(`Found requesting user task assigned to "${requestingUserId}".`);

        return true;
      }

      // Fallback: search by def name
      setStatus('No assigned task found. Searching by definition name...');
      const alt = await listUserTasks({ userTaskDefName: 'it-request' });
      setResponse(JSON.stringify(alt, null, 2));

      const candidates = extractTaskIds(alt);
      const match = candidates.find((t) => t.wfRunId.id === wfRunId?.id);

      if (!match) {
        setRequestingUserTaskGuid('');
        setStatus('No requesting user task found for this wfRunId.');

        return false;
      }

      setStatus(`Found unassigned requesting task. Assigning to "${requestingUserId}"...`);

      try {
        const result = await assignUserTask(match.wfRunId, match.userTaskGuid, { 
          userId: requestingUserId, 
          overrideClaim: true 
        });
        
        setResponse(JSON.stringify(result, null, 2));
      } catch (err) {
        
        setResponse(JSON.stringify({ error: err }, null, 2));
      }
      setRequestingUserTaskGuid(match.userTaskGuid);
      setStatus(`Assigned requesting task to "${requestingUserId} (found by def)".`);

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setRequestingUserTaskGuid('');
      setStatus(`Failed to find/assign requesting user task: ${message}`);
      
      return false;
    } finally {
      setLoading(false);
    }
  }, [requestingUserId, wfRunId, extractTaskIds, setLoading, setStatus, setResponse, setRequestingUserTaskGuid]);

  const completeRequestingTask = useCallback(async () => {
    setLoading(true);
    setResponse('');

    try {
      const results: Record<string, VariableValue> = {
        requestedItem: createVariableValue('STR', requestedItem),
        justification: createVariableValue('STR', justification)
      };

      if (!wfRunId) {
        throw new Error('wfRunId is required');
      }
      await completeUserTask(wfRunId, requestingUserTaskGuid, requestingUserId, results);

      setResponse(JSON.stringify({ ok: true }, null, 2));
      setStatus('OK');
      setRequestingTaskSubmitted(true);

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Error');

      return false;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, requestingUserTaskGuid, requestingUserId, requestedItem, justification,
    setLoading, setResponse, setStatus, setRequestingTaskSubmitted]);

  const findFinanceTask = useCallback(async () => {
    setLoading(true);
    setStatus('Finding Finance task... Looking for a pending Finance review task.');
    setResponse('');

    try {
      const data = await listUserTasks({ userGroup: 'finance' });
      setResponse(JSON.stringify(data, null, 2));

      const candidates = extractTaskIds(data);
      const match = candidates.find((t) => t.wfRunId.id === wfRunId?.id);
      
      if (match) {
        setFinanceUserTaskGuid(match.userTaskGuid);
        setStatus(`Found Finance task awaiting action.`);

        return true;
      } else {
        setFinanceUserTaskGuid('');
        setStatus('No Finance task found at this time.');

        return false;
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setFinanceUserTaskGuid('');
      setStatus(`Failed to find Finance task: ${message}`);

      return false;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, extractTaskIds, setLoading, setStatus, setResponse, setFinanceUserTaskGuid]);

  const assignFinanceTask = useCallback(async () => {
    setLoading(true);
    setStatus('Assigning task...');
    setResponse('');

    try {
      if (!wfRunId) {
        throw new Error('wfRunId is required');
      }
      await assignUserTask(wfRunId, financeUserTaskGuid, { 
        userId: financeAssigneeUserId, 
        overrideClaim: financeOverride 
      });
      
      setResponse(JSON.stringify({ 
        ok: true, 
        wfRunId, 
        userId: financeAssigneeUserId, 
        userTaskGuid: financeUserTaskGuid 
      }, null, 2));

      setStatus(`OK — Task assigned to ${financeAssigneeUserId}`);
      setFinanceAssigned(true);

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Error');
      setFinanceAssigned(false);

      return false;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, financeUserTaskGuid, financeAssigneeUserId, financeOverride,
      setLoading, setStatus, setResponse, setFinanceAssigned]);

  const completeFinanceTask = useCallback(async () => {
    setLoading(true);
    setResponse('');

    try {
      const results: Record<string, VariableValue> = {
        isApproved: createVariableValue('BOOL', financeDecision === 'APPROVE')
      };

      if (!wfRunId) {
        throw new Error('wfRunId is required');
      }

      await completeUserTask(wfRunId, financeUserTaskGuid, financeAssigneeUserId, results);
      setResponse(JSON.stringify({ ok: true }, null, 2));

      setStatus('OK');
      setWorkflowCompleted(true);
      setShowResultModal(true);

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus('Error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [wfRunId, financeUserTaskGuid, financeAssigneeUserId, financeDecision,
      setLoading, setResponse, setStatus, setWorkflowCompleted, setShowResultModal]);

  const restart = useCallback(async () => {
    setLoading(true);
    setResponse('');
    let hasDeleteOperation = false;
    
    try {
      if (deleteScope === 'current' && wfRunId) {
        const result = await deleteWfRun(wfRunId);

        setResponse(JSON.stringify(result, null, 2));
        setStatus('Deleted current wfRun.');
        hasDeleteOperation = true;
      } else if (deleteScope === 'all') {
        const result = await deleteAllWfRunsForSpec('it-request');

        setResponse(JSON.stringify(result, null, 2));
        setStatus(`Deleted ${result.deleted} wfRun(s) for spec "it-request".`);
        hasDeleteOperation = true;
      }

      if (hasDeleteOperation) {
        await new Promise(resolve => setTimeout(resolve, 3000));
      }

      reset();
      setStatus('Restarted.');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';

      setResponse(JSON.stringify({ error: message }, null, 2));
      setStatus(`Failed to restart: ${message}`);
    } finally {
      setLoading(false);
    }
  }, [deleteScope, wfRunId, setLoading, setResponse, setStatus, reset]);

  return {
    runWorkflow,
    findRequestingTask,
    completeRequestingTask,
    findFinanceTask,
    assignFinanceTask,
    completeFinanceTask,
    restart
  };
};