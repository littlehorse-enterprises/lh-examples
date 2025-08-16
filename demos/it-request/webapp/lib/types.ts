// Import types from littlehorse-client
import type { 
  UserTaskRun,
  UserTaskDef,
  UserTaskRunStatus,
  WfRunId,
  WfRun,
  UserTaskRunId,
  UserTaskRunIdList
} from 'littlehorse-client/proto';

// Re-export the types we need
export type {
  UserTaskRun,
  UserTaskDef,
  WfRunId,
  UserTaskRunId,
  UserTaskRunIdList,
  UserTaskRunStatus
};

// RunWfResponse is actually WfRun
export type RunWfResponse = WfRun;

// Custom types specific to our application
export type StepNumber = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export type FinanceDecision = 'APPROVE' | 'DECLINE';

export interface TaskIdRef {
  wfRunId: string;
  userTaskGuid: string;
}

export interface UserTaskDetails {
  userTaskRun: UserTaskRun;
  userTaskDef: UserTaskDef;
}

export type UserTaskFieldValue = string | boolean | number;

export interface HealthResponse {
  ok: boolean;
}

// Use the actual UserTaskRunIdList structure
export type TaskSearchResult = UserTaskRunIdList;
