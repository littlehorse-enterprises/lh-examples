'use server';

import { getClient } from '@/lib/lh-client';

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