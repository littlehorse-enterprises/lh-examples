'use server';

import { getClient } from './lh-client';
import { createVariableValue } from './utils';

import { 
  type WfRun,
  type UserTaskRunIdList,
  type WfRunId,
} from './types';

export async function startItRequest(userId: string): Promise<WfRun> {
  const client = getClient();
  const run = await client.runWf({
    wfSpecName: 'it-request',
    variables: { 
      'user-id': createVariableValue('str', userId)
    }
  });
  return run;
}

export async function listUserTasks(params: {
  userId?: string;
  userGroup?: string;
  status?: string;
  userTaskDefName?: string;
}): Promise<UserTaskRunIdList> {
  const client = getClient();
  return client.searchUserTaskRun({
    userId: params.userId,
    userGroup: params.userGroup,
    status: params.status as any,
    userTaskDefName: params.userTaskDefName,
    limit: 50
  });
}

export async function assignUserTask(
  wfRunId: WfRunId,
  userTaskGuid: string,
  body: {
    userId?: string;
    userGroup?: string;
    override?: boolean;
  }
): Promise<void> {
  const client = getClient();

  try {
    await client.assignUserTaskRun({
      userTaskRunId: { wfRunId, userTaskGuid },
      overrideClaim: Boolean(body.override),
      userId: body.userId,
      userGroup: body.userGroup
    });
  } catch (err) {
    throw err;
  }
}