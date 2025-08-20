import type { 
  UserTaskRun,
  UserTaskDef,
  UserTaskRunStatus,
  WfRunId,
  WfRun,
  UserTaskRunId,
  UserTaskRunIdList,
  VariableValue,
  AssignUserTaskRunRequest,
  CompleteUserTaskRunRequest,
  RunWfRequest,
  SearchUserTaskRunRequest,
  DeleteWfRunRequest,
  SearchWfRunRequest,
} from 'littlehorse-client/dist/proto';

export type {
  UserTaskRun,
  UserTaskDef,
  UserTaskRunStatus,
  WfRunId,
  WfRun,
  UserTaskRunId,
  UserTaskRunIdList,
  VariableValue,
  AssignUserTaskRunRequest,
  CompleteUserTaskRunRequest,
  RunWfRequest,
  SearchUserTaskRunRequest,
  DeleteWfRunRequest,
  SearchWfRunRequest,
};

export type StepNumber = 1 | 2 | 3 | 4 | 5 | 6 | 7;
export type FinanceDecision = 'APPROVE' | 'DECLINE';

export interface TaskIdRef {
  wfRunId: WfRunId;
  userTaskGuid: string;
};

export interface UserTaskDetails {
  userTaskRun: UserTaskRun;
  userTaskDef: UserTaskDef;
};

export type UserTaskFieldValue = string | boolean | number;