"use server";

import client from "../client";

export const checkout = async (cartId: string) => {
  return client.putExternalEvent({
    wfRunId: { id: cartId },
    externalEventDefId: { name: "checkout-completed" },
  });
};
