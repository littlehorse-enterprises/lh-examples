import { getClient } from '@/lib/lh-client';
import type { WfRunId } from 'littlehorse-client/proto';
import type { UserTaskDetails } from '@/lib/types';

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