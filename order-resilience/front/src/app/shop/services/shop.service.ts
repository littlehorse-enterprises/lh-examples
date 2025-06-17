import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Product } from '../models/product.model';

export interface CartItem {
  productId: number;
  quantity: number;
  discountCode?: string;
  discountApplied?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  private cartItemsSubject: BehaviorSubject<Map<number, CartItem>> = new BehaviorSubject<Map<number, CartItem>>(new Map());
  public cartItems$: Observable<Map<number, CartItem>> = this.cartItemsSubject.asObservable();
  
  constructor() {}
  
  public getCart(): Map<number, CartItem> {
    return this.cartItemsSubject.value;
  }
  
  public addToCart(product: Product): void {
    const cart = new Map(this.cartItemsSubject.value);
    const currentItem = cart.get(product.productId);
    const currentQuantity = currentItem ? currentItem.quantity : 0;
    
    if (currentQuantity < product.quantity) {
      if (currentItem) {
        cart.set(product.productId, {
          ...currentItem,
          quantity: currentQuantity + 1
        });
      } else {
        cart.set(product.productId, {
          productId: product.productId,
          quantity: 1
        });
      }
      this.cartItemsSubject.next(cart);
    }
  }
  
  public removeFromCart(productId: number): void {
    const cart = new Map(this.cartItemsSubject.value);
    const currentItem = cart.get(productId);
    
    if (currentItem && currentItem.quantity > 0) {
      if (currentItem.quantity === 1) {
        cart.delete(productId);
      } else {
        cart.set(productId, {
          ...currentItem,
          quantity: currentItem.quantity - 1
        });
      }
      this.cartItemsSubject.next(cart);
    }
  }
  
  public getCartQuantity(productId: number): number {
    const item = this.cartItemsSubject.value.get(productId);
    return item ? item.quantity : 0;
  }
  
  public setDiscountCode(productId: number, code: string): void {
    const cart = new Map(this.cartItemsSubject.value);
    const currentItem = cart.get(productId);
    
    if (currentItem) {
      cart.set(productId, {
        ...currentItem,
        discountCode: code,
        discountApplied: code.trim() !== ''
      });
      this.cartItemsSubject.next(cart);
    }
  }
  
  public clearDiscountCode(productId: number): void {
    const cart = new Map(this.cartItemsSubject.value);
    const currentItem = cart.get(productId);
    
    if (currentItem) {
      cart.set(productId, {
        ...currentItem,
        discountCode: '',
        discountApplied: false
      });
      this.cartItemsSubject.next(cart);
    }
  }
  
  public clearCart(): void {
    this.cartItemsSubject.next(new Map());
  }
  
  public getCartItemCount(): number {
    let count = 0;
    this.cartItemsSubject.value.forEach(item => {
      count += item.quantity;
    });
    return count;
  }
  
  public getAppliedDiscountCodes(): string[] {
    const discountCodes: string[] = [];
    this.cartItemsSubject.value.forEach(item => {
      if (item.discountApplied && item.discountCode) {
        discountCodes.push(item.discountCode);
      }
    });
    return discountCodes;
  }
  
  public getCartTotal(products: Product[]): number {
    let total = 0;
    this.cartItemsSubject.value.forEach((item) => {
      const product = products.find(p => p.productId === item.productId);
      if (product) {
        total += product.unitPrice * item.quantity;
      }
    });
    return total;
  }
  
  public getDiscountAmount(products: Product[]): number {
    let discount = 0;
    this.cartItemsSubject.value.forEach((item) => {
      if (item.discountApplied) {
        const product = products.find(p => p.productId === item.productId);
        if (product) {
          // Apply a 10% discount for demonstration purposes
          discount += (product.unitPrice * item.quantity) * 0.1;
        }
      }
    });
    return discount;
  }
  
  public getFinalTotal(products: Product[]): number {
    return this.getCartTotal(products) - this.getDiscountAmount(products);
  }
}
