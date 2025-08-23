'use server';

import { getClient } from '@/lib/lh-client';
import type { WfRunId, VariableValue } from 'littlehorse-client/proto';

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