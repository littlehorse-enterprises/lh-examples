'use server';

import { getClient } from '@/lib/lh-client';
import type { UserTaskRunIdList, UserTaskRunStatus } from 'littlehorse-client/proto';

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