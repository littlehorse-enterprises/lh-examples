"use server";

import client from "../client";
import { Product } from "../components/Shop";

export const updateProducts = async (cartId: string, products: Product[]) => {
  return client.putExternalEvent({
    wfRunId: { id: cartId },
    externalEventDefId: { name: "add-to-cart" },
    content: {
      jsonArr: JSON.stringify(products),
    },
  });
};
