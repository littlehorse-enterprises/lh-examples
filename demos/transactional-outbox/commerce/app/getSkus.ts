"use server";
export type Account = {
  account: string;
  balance: number;
};


export async function getSkus(): Promise<string[]> {
  const warehouseApi = process.env.WAREHOUSE_API || "http://localhost:8082";
  const res = await fetch(`${warehouseApi}/balance`);

  if (!res.ok) {
    throw new Error("Failed to fetch data");
  }

  const accounts = (await res.json()) as Account[];

  return accounts.map(({ account } )=> (
    account
  ))
}
