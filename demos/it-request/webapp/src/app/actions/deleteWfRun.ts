'use server';

import { getClient } from '@/lib/lh-client';
import type { WfRunId } from 'littlehorse-client/proto';

export async function deleteWfRun(wfRunId: WfRunId): Promise<void> {
  const client = getClient();
  await client.deleteWfRun({ id: wfRunId });
}