"use server";

import client from "../client";

export const createCart = async (email: string) => {
  const response = await client.runWf({
    wfSpecName: "cart-management",
    variables: { account: { str: email } },
  });
  return response.id;
};
