'use server';

import { getClient } from './lh-client';

import type {
  TaskSearchResult,
  UserTaskDetails,
  UserTaskFieldValue,
  RunWfResponse,
  HealthResponse
} from './types';

export async function health(): Promise<HealthResponse> {
  try {
    // In a real implementation, you might want to ping the LH server
    const client = getClient();
    // Try to make a simple call to verify connectivity
    await client.searchWfSpec({ limit: 1 });
    return { ok: true };
  } catch (error) {
    console.error('Health check failed:', error);
    console.error('Environment variables:');
    console.error('LHC_API_HOST:', process.env.LHC_API_HOST || 'localhost');
    console.error('LHC_API_PORT:', process.env.LHC_API_PORT || '2023');
    console.error('LHC_API_PROTOCOL:', process.env.LHC_API_PROTOCOL || 'PLAINTEXT');
    return { ok: false };
  }
}

export async function startItRequest(userId: string): Promise<RunWfResponse> {
  const client = getClient();
  const run = await client.runWf({
    wfSpecName: 'it-request',
    variables: { 'user-id': { str: String(userId) } }
  });
  return run;
}

export async function getUserTask(wfRunId: string, userTaskGuid: string): Promise<UserTaskDetails> {
  const client = getClient();
  const userTaskRun = await client.getUserTaskRun({ 
    wfRunId: { id: wfRunId }, 
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
  status?: string;
  userTaskDefName?: string;
}): Promise<TaskSearchResult> {
  const client = getClient();
  const result = await client.searchUserTaskRun({
    userId: params.userId,
    userGroup: params.userGroup,
    status: params.status as any, // UserTaskRunStatus is a string enum
    userTaskDefName: params.userTaskDefName,
    limit: 50
  });

  // Return the result directly as it matches UserTaskRunIdList
  return result;
}

export async function assignUserTask(
  wfRunId: string,
  userTaskGuid: string,
  body: {
    userId?: string;
    userGroup?: string;
    override?: boolean;
  }
): Promise<void> {
  const client = getClient();
  await client.assignUserTaskRun({
    userTaskRunId: { wfRunId: { id: wfRunId }, userTaskGuid },
    overrideClaim: Boolean(body.override),
    userId: body.userId,
    userGroup: body.userGroup
  });
}

export async function completeUserTask(
  wfRunId: string,
  userTaskGuid: string,
  userId: string,
  results: Record<string, UserTaskFieldValue>
): Promise<void> {
  const client = getClient();
  
  // Get the user task definition to validate fields
  const utr = await client.getUserTaskRun({ 
    wfRunId: { id: wfRunId }, 
    userTaskGuid 
  });
  
  const utdId = utr.userTaskDefId;
  if (!utdId) {
    throw new Error('userTaskDefId missing on UserTaskRun');
  }
  
  const utd = await client.getUserTaskDef(utdId);
  
  // Convert results to the proper format
  const finalResults: Record<string, any> = {};
  for (const field of utd.fields) {
    const raw = results[field.name];
    if (raw === undefined || raw === null) {
      if (field.required) {
        throw new Error(`field ${field.name} is required`);
      }
      continue;
    }
    
    // Convert based on field type
    switch (field.type) {
      case 'STR':
        finalResults[field.name] = { str: String(raw) };
        break;
      case 'BOOL':
        finalResults[field.name] = { bool: raw === true || raw === 'true' };
        break;
      case 'INT':
        finalResults[field.name] = { int: Number.parseInt(String(raw), 10) };
        break;
      case 'DOUBLE':
        finalResults[field.name] = { double: Number(raw) };
        break;
      default:
        finalResults[field.name] = { str: String(raw) };
    }
  }

  await client.completeUserTaskRun({
    userTaskRunId: { wfRunId: { id: wfRunId }, userTaskGuid },
    results: finalResults,
    userId
  });
}

export async function deleteWfRun(wfRunId: string): Promise<void> {
  const client = getClient();
  await client.deleteWfRun({ id: { id: wfRunId } });
}

export async function deleteAllWfRunsForSpec(wfSpecName: string): Promise<{ deleted: number }> {
  const client = getClient();
  let bookmark: Buffer | undefined = undefined;
  let totalDeleted = 0;

  // Paginate through all WfRuns for the given spec and delete them
  for (;;) {
    const list: any = await client.searchWfRun({ 
      wfSpecName, 
      limit: 100, 
      ...(bookmark ? { bookmark } : {}) 
    });
    
    const results: Array<{ id: string } | { id: { id: string } }> = list?.results || [];
    
    for (const item of results) {
      const idStr: string | undefined = typeof (item as any).id === 'string'
        ? (item as any).id
        : (item as any).id?.id;
      
      if (idStr) {
        await client.deleteWfRun({ id: { id: idStr } });
        totalDeleted += 1;
      }
    }
    
    const next: Buffer | undefined = list?.bookmark;
    if (!next || results.length === 0) break;
    bookmark = next;
  }

  return { deleted: totalDeleted };
}
