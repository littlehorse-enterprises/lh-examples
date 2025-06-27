export interface Coupon {
  clientId: number;
  productId: number;
  code: string;
  description: string;
  discountPercentage: number;
  redeemed: boolean;
}
