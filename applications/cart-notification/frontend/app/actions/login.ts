"use server";

import { ToastProps } from "@/app/components/Toast";
import { cookies } from "next/headers";
import { createCart } from "./createCart";

export async function login(
  _state: ToastProps,
  formData: FormData
): Promise<ToastProps> {
  const email = formData.get("email")?.toString();

  if (email) {
    const wfRun = await createCart(email);
    cookies().set("email", email);
    if (wfRun?.id) {
      cookies().set("cartId", wfRun.id);
    }
    return { message: "Logged in", variant: "success" };
  } else {
    return { message: "Email invalid", variant: "error" };
  }
}
