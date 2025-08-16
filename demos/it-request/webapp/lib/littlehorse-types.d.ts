// Workaround for broken TypeScript definitions in littlehorse-client
declare module 'littlehorse-client/proto' {
  export interface UserTaskRun {
    id?: UserTaskRunId;
    userTaskDefId?: UserTaskDefId;
    userGroup?: string;
    userId?: string;
    results?: { [key: string]: any };
    status: UserTaskRunStatus;
    events: UserTaskEvent[];
    notes?: string;
    scheduledTime?: any;
    nodeRunId?: any;
  }

  export interface UserTaskDef {
    name: string;
    version: number;
    fields: UserTaskField[];
    description?: string;
  }

  export interface UserTaskField {
    name: string;
    type: any;
    description?: string;
    displayName?: string;
    required?: boolean;
  }

  export interface UserTaskEvent {
    time?: any;
    taskExecuted?: any;
    assigned?: any;
    cancelled?: any;
    saved?: any;
  }

  export enum UserTaskRunStatus {
    UNASSIGNED = "UNASSIGNED",
    ASSIGNED = "ASSIGNED", 
    DONE = "DONE",
    CANCELLED = "CANCELLED",
    UNRECOGNIZED = "UNRECOGNIZED"
  }

  export interface WfRunId {
    id: string;
  }

  export interface UserTaskRunId {
    wfRunId?: WfRunId;
    userTaskGuid: string;
  }

  export interface UserTaskDefId {
    name: string;
    version: number;
  }

  export interface WfRun {
    id?: WfRunId;
    wfSpecId?: any;
    oldWfSpecVersions?: any[];
    status: any;
    threadRuns: any[];
    pendingInterrupts: any[];
    pendingFailures: any[];
  }

  export interface UserTaskRunIdList {
    results: UserTaskRunId[];
    bookmark?: any;
  }
}
