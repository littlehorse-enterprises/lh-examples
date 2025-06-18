import { Product } from "../shop/models/product.model";

export interface CartItem {
    productId: number;
    quantity: number;
    product:Product;
    discountPercentage: number;
    discountCode?: string;
}

export interface Cart {
    items: CartItem[];
    subtotal: number;
    discount: number;
    total: number;
}