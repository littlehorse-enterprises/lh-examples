"use server";

import { revalidateTag } from "next/cache";

export default async function revalidate(sku: string) {
  revalidateTag(sku);
}
