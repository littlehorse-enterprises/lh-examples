import express from 'express';
import { getClient } from './lh.js';
import { userTaskRunStatusFromJSON } from 'littlehorse-client/proto';

const app = express();
app.use(express.json());

app.get('/api/health', (_req, res) => res.json({ ok: true }));

app.post('/api/run/it-request', async (req, res) => {
    const { userId } = req.body || {};
    if (!userId) return res.status(400).json({ error: 'userId is required' });
    const client = getClient();
    const run = await client.runWf({
        wfSpecName: 'it-request',
        variables: { 'user-id': { str: String(userId) } }
    });
    res.json(run);
});

app.get('/api/user-tasks', async (req, res) => {
    const client = getClient();
    const { userId, userGroup, status, userTaskDefName } = req.query;
    const result = await client.searchUserTaskRun({
        userId: typeof userId === 'string' ? userId : undefined,
        userGroup: typeof userGroup === 'string' ? userGroup : undefined,
        status: typeof status === 'string' ? userTaskRunStatusFromJSON(status) : undefined,
        userTaskDefName: typeof userTaskDefName === 'string' ? userTaskDefName : undefined,
        limit: 50
    });
    res.json(result);
});

app.get('/api/user-task/:wfRunId/:userTaskGuid', async(req, res) => {
    const client = getClient();
    const { wfRunId, userTaskGuid } = req.params;
    const userTaskRun = await client.getUserTaskRun({ wfRunId: { id: wfRunId }, userTaskGuid });
    const utdId = userTaskRun.userTaskDefId;
    if (!utdId) return res.status(500).json({ error: 'userTaskDefId missing on UserTaskRun' });
    const userTaskDef = await client.getUserTaskDef(utdId);
    res.json({ userTaskRun, userTaskDef });
});

function toVar(value: any, type: string) {
    switch (type) {
        case 'STR': return { str: String(value) };
        case 'BOOL': return { bool: value === true || value === 'true' }
        case 'INT': return { int: Number.parseInt(String(value), 10) }
        case 'DOUBLE': return { double: Number(value) }
        default: return { str: String(value) }
    }
}

app.post('/api/user-task/:wfRunId/:userTaskGuid/complete', async (req, res) => {
    const client = getClient();
    const { wfRunId, userTaskGuid } = req.params;
    const { userId, results } = req.body || {};
    if (!userId || !results) return res.status(400).json({ error: 'userId and results are required' });

    const utr = await client.getUserTaskRun({ wfRunId: { id: wfRunId }, userTaskGuid });
    const utdId = utr.userTaskDefId;
    if (!utdId) return res.status(500).json({ error: 'userTaskDefId missing on UserTaskRun' });
    const utd = await client.getUserTaskDef(utdId);

    const finalResults: Record<string, any> = {};
    for (const field of utd.fields) {
        const raw = results[field.name];
        if (raw === undefined || raw === null) {
            if (field.required) return res.status(400).json({ error: `field ${field.name} is required` });
            continue;
        }
        finalResults[field.name] = toVar(raw, field.type);
    }

    await client.completeUserTaskRun({
        userTaskRunId: { wfRunId: { id: wfRunId }, userTaskGuid },
        results: finalResults,
        userId
    });
    res.status(204).end();
});

app.post('/api/user-task/:wfRunId/:userTaskGuid/assign', async (req, res) => {
    const client = getClient();
    const  { wfRunId, userTaskGuid } = req.params;
    const { userId, userGroup, override } = req.body || {};
    await client.assignUserTaskRun({
        userTaskRunId: { wfRunId: { id: wfRunId }, userTaskGuid },
        overrideClaim: Boolean(override), // TODO: check how override actually works
        userId,
        userGroup
    });
    res.status(204).end();
});

// Admin: delete a single WfRun by id
app.post('/api/admin/delete-wfrun/:wfRunId', async (req, res) => {
    const client = getClient();
    const { wfRunId } = req.params;
    if (!wfRunId) return res.status(400).json({ error: 'wfRunId is required' });
    await client.deleteWfRun({ id: { id: wfRunId } });
    res.status(204).end();
});

// Admin: delete all WfRuns for a given WfSpec name (current example uses "it-request")
app.post('/api/admin/delete-wfruns/:wfSpecName', async (req, res) => {
    const client = getClient();
    const { wfSpecName } = req.params;
    if (!wfSpecName) return res.status(400).json({ error: 'wfSpecName is required' });

    let bookmark: Buffer | undefined = undefined;
    let totalDeleted = 0;
    // Paginate through all WfRuns for the given spec and delete them
    for (;;) {
        const list: any = await client.searchWfRun({ wfSpecName, limit: 100, ...(bookmark ? { bookmark } : {}) });
        const results: Array<{ id: string } | { id: { id: string } }> = (list?.results as any[]) || [];
        for (const item of results) {
            const idStr: string | undefined = typeof (item as any).id === 'string'
                ? (item as any).id
                : (item as any).id?.id;
            if (idStr) {
                await client.deleteWfRun({ id: { id: idStr } });
                totalDeleted += 1;
            }
        }
        const next: Buffer | undefined = list?.bookmark as Buffer | undefined;
        if (!next || results.length === 0) break;
        bookmark = next;
    }

    res.json({ deleted: totalDeleted });
});

const port = process.env.PORT || 4000;
app.listen(port, () => console.log(`API listening on http://localhost:${port}`));