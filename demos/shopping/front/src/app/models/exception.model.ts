import { Product } from "./product.model";

export interface StockException {
  clientId: number;
  products: Product[];
  message: string;
}
