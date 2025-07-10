export interface Product {
  productId: number;
  name: string;
  description: string;
  unitPrice: number; // Same as unitPrice
  cost: number;
  quantity: number; // Same as availableStock
  category: string;
  
  // UI fields
  availableStock?: number; // For backwards compatibility
  
  // Additional fields from backend that we might use later  
  discountPercentage?: number;
  requestedQuantity?: number;
}
