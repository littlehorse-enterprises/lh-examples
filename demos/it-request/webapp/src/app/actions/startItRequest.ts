'use server';

import { getClient } from '@/lib/lh-client';
import { createVariableValue } from '@/lib/utils';
import type { WfRun } from 'littlehorse-client/proto';

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