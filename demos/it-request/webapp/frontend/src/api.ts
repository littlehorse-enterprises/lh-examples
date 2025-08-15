const env = (import.meta as { env: { DEV: boolean; VITE_API_URL?: string } }).env;
// In development, always use relative URLs so Vite's proxy handles CORS.
const BASE: string = env.DEV ? '' : (env.VITE_API_URL || '');

export type TaskIdRef = {
    wfRunId: string;
    userTaskGuid: string;
};

export type TaskSearchResult = {
    ids: {
        ids: TaskIdRef[];
    };
};

export type UserTaskFieldType = 'STR' | 'BOOL' | 'INT' | 'DOUBLE';

export type UserTaskField = {
    name: string;
    displayName: string;
    type: UserTaskFieldType;
    description?: string;
    required?: boolean;
};

export type UserTaskDef = {
    name: string;
    version: number;
    fields: UserTaskField[];
};

export type UserTaskRun = {
    notes?: string;
    userGroup?: string;
    userId?: string;
};

export type UserTaskDetails = {
    userTaskRun: UserTaskRun;
    userTaskDef: UserTaskDef;
};

export type HealthResponse = { ok: boolean };

export type RunWfResponse = {
    id: {
        id: string;
    };
};

export type UserTaskFieldValue = string | boolean | number;

export async function health(): Promise<HealthResponse> {
    const response = await fetch(`${BASE}/api/health`);
    if (!response.ok) {
        throw new Error(await response.text());
    }
    const data: HealthResponse = await response.json();
    return data;
}

export async function startItRequest(userId: string): Promise<RunWfResponse> {
    const response = await fetch(`${BASE}/api/run/it-request`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId })
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    const data: RunWfResponse = await response.json();
    return data;
};

export async function getUserTask(wfRunId: string, userTaskGuid: string): Promise<UserTaskDetails> {
    const response = await fetch(`${BASE}/api/user-task/${wfRunId}/${userTaskGuid}`);

    if (!response.ok) {
        throw new Error(await response.text());
    }

    const data: UserTaskDetails = await response.json();
    return data;
};

export async function listUserTasks(params: { userId?: string; userGroup?: string; status?: string; userTaskDefName?: string; }): Promise<TaskSearchResult> {
    const query = new URLSearchParams();
    params.userId && query.set('userId', params.userId);
    params.userGroup && query.set('userGroup', params.userGroup);
    params.status && query.set('status', params.status);
    params.userTaskDefName && query.set('userTaskDefName', params.userTaskDefName);

    const response = await fetch(`${BASE}/api/user-tasks?${query.toString()}`);

    if (!response.ok) {
        throw new Error(await response.text());
    }

    const data: TaskSearchResult = await response.json();
    return data;
};

export async function assignUserTask(wfRunId: string, userTaskGuid: string, body: {
    userId?: string;
    userGroup?: string;
    override?: boolean;
}): Promise<void> {
    const response = await fetch(`${BASE}/api/user-task/${wfRunId}/${userTaskGuid}/assign`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    // 204 No Content
    return;
};

export async function completeUserTask(
    wfRunId: string,
    userTaskGuid: string,
    userId: string,
    results: Record<string, UserTaskFieldValue>
): Promise<void> {
    const response = await fetch(`${BASE}/api/user-task/${wfRunId}/${userTaskGuid}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, results })
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    // 204 No Content
    return;
};

export async function deleteWfRun(wfRunId: string): Promise<void> {
  const response = await fetch(`${BASE}/api/admin/delete-wfrun/${wfRunId}`, { method: 'POST' });
  if (!response.ok) {
    throw new Error(await response.text());
  }
}

export async function deleteAllWfRunsForSpec(wfSpecName: string): Promise<{ deleted: number }> {
  const response = await fetch(`${BASE}/api/admin/delete-wfruns/${encodeURIComponent(wfSpecName)}`, { method: 'POST' });
  if (!response.ok) {
    throw new Error(await response.text());
  }
  return await response.json();
}