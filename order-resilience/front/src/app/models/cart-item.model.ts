import { Product } from "../shop/models/product.model";

export interface CartItem {
    productId: number;
    quantity: number;
    product:Product;
    discountCode?: string;
    discountApplied?: boolean;
}