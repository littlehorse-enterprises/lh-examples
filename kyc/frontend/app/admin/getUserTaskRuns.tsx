"use server";

import { UserTaskRun } from "littlehorse-client/dist/proto/user_tasks";
import client from "../client";

export const getUserTaskRuns = async (): Promise<UserTaskRun[]> => {
  const { results } = await client.searchUserTaskRun({
    userGroup: "support",
  });

  let userTaskRuns: UserTaskRun[] = [];

  for (let { wfRunId, userTaskGuid } of results) {
    const userTaskRun = await client.getUserTaskRun({
      wfRunId,
      userTaskGuid,
    });

    if (["UNASSIGNED", "ASSIGNED"].includes(userTaskRun.status)) {
      userTaskRuns = [...userTaskRuns, userTaskRun];
    }
  }

  return userTaskRuns;
};
