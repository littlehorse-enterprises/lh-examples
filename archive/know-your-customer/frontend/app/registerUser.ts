"use server";

import { ToastProps } from "@/components/Toast";
import client from "./client";


export async function registerUser(
  _state: ToastProps,
  formData: FormData
): Promise<ToastProps> {
  const user = {
    firstname: formData.get("firstname"),
    lastname: formData.get("lastname"),
    email: formData.get("email"),
  };

  try {
    await client.runWf({
      wfSpecName: "kyc",
      variables: {
        user: {
          jsonObj: JSON.stringify(user),
        },
      },
    });
    return {
      message:
        "User created, please check your email to proceed with verification",
      variant: "success"
    };
  } catch (error) {
    return { message: "Something really bad happened", variant: "error" };
  }
}
