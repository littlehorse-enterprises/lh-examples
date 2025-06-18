import { Product } from "./product.model";

export interface OrderRequest {
    clientId?: number;
    orderLines: OrderLineRequest[];
    discountCodes: string[];
}

export interface OrderLineRequest {
    productId: number;
    quantity: number;
}

export interface OrderResponse {
    orderId: number;
    clientId: number;
    message: string;
    total: number;
    status?: 'PENDING' | string; // default is "PENDING", but it may change
    orderLines: OrderLineResponse[];
    discountCodes: string[];
    creationDate: Date;
}

export interface OrderLineResponse {
    productId: number;
    quantity: number;
    unitPrice: number;
    discountPercentage: number;
    totalPrice: number;
    product?: Product
}