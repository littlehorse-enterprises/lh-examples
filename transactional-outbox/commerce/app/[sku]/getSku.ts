import { Account } from "../getSkus";

type Product = {
  sku: String;
  stock: number;
};

export async function getSku(sku: String): Promise<Product> {
  const warehouseApi = process.env.WAREHOUSE_API || "http://localhost:8082";
  const res = await fetch(`${warehouseApi}/balance/${sku}`);

  if (!res.ok) {
    throw new Error("Failed to fetch data");
  }

  const { account, balance } = (await res.json()) as Account;

  return {
    sku: account,
    stock: balance
  };
}
