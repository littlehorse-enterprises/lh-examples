"use server";

import client from "@/app/client";
import { ToastProps } from "@/components/Toast";

type State = {
  wfRunId: string;
} & ToastProps;

export async function uploadPassport(
  state: State,
  formData: FormData
): Promise<State> {

  const legit = !!formData.get("legit")

  try {
    await client.putExternalEvent({
      wfRunId: {
        id: state.wfRunId,
      },
      externalEventDefId: {
        name: "passport-submitted",
      },
      content: {
        jsonObj: JSON.stringify({ legit }),
      },
    });
    return {
      ...state,
      message:
        "Passport uploaded correctly, please check your email",
      variant: "success"
    };
  } catch (error) {
    console.log(error)
    return { ...state, message: "Something really bad happened", variant: "error" };
  }
}
