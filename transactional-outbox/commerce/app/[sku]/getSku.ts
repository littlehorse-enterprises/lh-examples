import { Account } from "../getSkus";
import seedrandom from 'seedrandom'

type Product = {
  sku: string;
  stock: number;
  price: number
};

export async function getSku(sku: string): Promise<Product> {
  const warehouseApi = process.env.WAREHOUSE_API || "http://localhost:8082";
  const res = await fetch(`${warehouseApi}/balance/${sku}`);

  if (!res.ok) {
    throw new Error("Failed to fetch data");
  }

  const { account, balance } = (await res.json()) as Account;

  return {
    sku: account,
    stock: balance,
    price: getRandomPrice(account)
  };
}

const getRandomPrice = (seed: string): number => {
  const random = seedrandom(seed).double()
  const min = 1;
  const max = 10;
  const diff = max - min;
  return parseFloat(((random * diff) + min).toFixed(2));
}
