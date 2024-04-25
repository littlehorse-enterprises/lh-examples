"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";

export async function logout() {
  cookies().delete("email");
  cookies().delete("cartId");

  revalidatePath("/");
}
