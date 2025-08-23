'use server';

import { getClient } from '@/lib/lh-client';
import type { WfRunId } from 'littlehorse-client/proto';
import { AssignUserTaskRunRequest } from 'littlehorse-client/proto';

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