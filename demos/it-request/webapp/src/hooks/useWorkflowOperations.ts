import { useCallback } from 'react';
import { useWorkflowStore } from '@/store/workflow.store';
import {
  startItRequest,
  listUserTasks,
  assignUserTask,
} from '@/lib/api'
import type { TaskIdRef } from '@/lib/types';

export const useWorkflowOperations = () => {
  const {
    requestingUserId,
    wfRunId,
    setWfRunId,
    setRequestingUserTaskGuid,
    setLoading,
    setStatus,
    setResponse,
  } = useWorkflowStore();

  const extractTaskIds = useCallback((value: unknown): TaskIdRef[] => {
    if (
      typeof value === 'object' && 
      value !== null &&
      'results' in (value as Record<string, unknown>) &&
      Array.isArray((value as Record<string, any>).results)
    ) {
      const arr: unknown[] = (value as Record<string, any>).results;
      return arr.map((item: any) => ({
        wfRunId: item.wfRunId,
        userTaskGuid: item.userTaskGuid,
      }));
    }
    return [];
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
      const queryParams = { userId: requestingUserId, status: 'ASSIGNED' };
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
          override: true 
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

  return {
    runWorkflow,
    findRequestingTask,
  };
};