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
  id: string;
  clientId: number;
  status: string;
  totalAmount: number;
  createdAt: string;
  orderLines: OrderLineResponse[];
}

export interface OrderLineResponse {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  discountAmount?: number;
}
