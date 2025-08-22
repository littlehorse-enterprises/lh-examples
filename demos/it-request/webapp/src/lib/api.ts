'use server';

import { getClient } from './lh-client';
import { createVariableValue } from './utils';
import { type UserTaskDetails } from './types';
import { AssignUserTaskRunRequest, UserTaskRunStatus, VariableValue } from 'littlehorse-client/proto';
import type { WfRun, UserTaskRunIdList, WfRunId } from 'littlehorse-client/proto';

export async function startItRequest(userId: string): Promise<WfRun> {
  const client = getClient();
  const run = await client.runWf({
    wfSpecName: 'it-request',
    variables: { 
      'user-id': createVariableValue('STR', userId)
    }
  });
  return run;
}

export async function getUserTask(wfRunId: WfRunId, userTaskGuid: string): Promise<UserTaskDetails> {
  const client = getClient();
  const userTaskRun = await client.getUserTaskRun({ 
    wfRunId, 
    userTaskGuid 
  });
  
  const utdId = userTaskRun.userTaskDefId;
  if (!utdId) {
    throw new Error('userTaskDefId missing on UserTaskRun');
  }
  
  const userTaskDef = await client.getUserTaskDef(utdId);
  return { userTaskRun, userTaskDef };
}

export async function listUserTasks(params: {
  userId?: string;
  userGroup?: string;
  status?: UserTaskRunStatus;
  userTaskDefName?: string;
}): Promise<UserTaskRunIdList> {
  const client = getClient();
  return client.searchUserTaskRun({
    userId: params.userId,
    userGroup: params.userGroup,
    status: params.status,
    userTaskDefName: params.userTaskDefName,
    limit: 50
  });
}

export async function assignUserTask(
  wfRunId: WfRunId,
  userTaskGuid: string,
  body: Partial<AssignUserTaskRunRequest>
): Promise<void> {
  const client = getClient();

  try {
    await client.assignUserTaskRun({
      userTaskRunId: { wfRunId, userTaskGuid },
      overrideClaim: Boolean(body.overrideClaim),
      userId: body.userId,
      userGroup: body.userGroup
    });
  } catch (err) {
    throw err;
  }
}

export async function completeUserTask(
  wfRunId: WfRunId,
  userTaskGuid: string,
  userId: string,
  results: Record<string, VariableValue>
): Promise<void> {
  const client = getClient();
  
  const utr = await client.getUserTaskRun({ wfRunId, userTaskGuid });
  const utdId = utr.userTaskDefId;
  if (!utdId) throw new Error('userTaskDefId missing on UserTaskRun');
  
  const utd = await client.getUserTaskDef(utdId);
  
  for (const field of utd.fields) {
    if (field.required && !results[field.name]) {
      throw new Error(`field ${field.name} is required`);
    }
  }

  await client.completeUserTaskRun({
    userTaskRunId: { wfRunId, userTaskGuid },
    results,
    userId
  });
}

export async function deleteWfRun(wfRunId: WfRunId): Promise<void> {
  const client = getClient();
  await client.deleteWfRun({ id: wfRunId });
}

export async function deleteAllWfRunsForSpec(wfSpecName: string): Promise<{ deleted: number }> {
  const client = getClient();
  let bookmark: Buffer | undefined = undefined;
  let totalDeleted = 0;

  for (;;) {
    const list = await client.searchWfRun({ 
      wfSpecName, 
      limit: 100, 
      ...(bookmark ? { bookmark } : {}) 
    });
    
    const results = list?.results || [];
    
    for (const wfRun of results) {
      if (wfRun.id) {
        await client.deleteWfRun({ id: { id: wfRun.id } });
        totalDeleted += 1;
      }
    }
    
    const next = list?.bookmark;
    if (!next || results.length === 0) break;
    bookmark = next;
  }

  return { deleted: totalDeleted };
}