"use server";

import client from "@/app/client";
import { revalidatePath } from "next/cache";

import { UserTaskRun } from "littlehorse-client/dist/proto/user_tasks";
import { ToastProps } from "@/components/Toast";

type State = {
  userTaskRun: UserTaskRun;
} & ToastProps;

export const verify = async (
  state: State,
  formData: FormData
): Promise<State> => {
  const userTaskRunId = state.userTaskRun.id;
  const approved = !!formData.get("approved");
  console.log("approved", approved);

  try {
    await client.completeUserTaskRun({
      userTaskRunId,
      results: {
        approved: {
          bool: approved,
        },
      },
    });
    revalidatePath("/admin");
    return {
      ...state,
      message: `User ${state.userTaskRun.notes} was ${approved ? "approved" : "rejected"}`,
      variant: "success",
    };
  } catch (error) {
    return {
      ...state,
      message: `Failed to process ${state.userTaskRun.notes}`,
      variant: "error",
    };
  }
};
