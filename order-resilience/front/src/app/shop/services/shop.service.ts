import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  private cartItemsSubject: BehaviorSubject<Map<number, number>> = new BehaviorSubject<Map<number, number>>(new Map());
  public cartItems$: Observable<Map<number, number>> = this.cartItemsSubject.asObservable();
  
  constructor() {}
  
  public getCart(): Map<number, number> {
    return this.cartItemsSubject.value;
  }
  
  public addToCart(product: Product): void {
    const cart = new Map(this.cartItemsSubject.value);
    const currentQuantity = cart.get(product.productId) || 0;
    
    if (currentQuantity < product.quantity) {
      cart.set(product.productId, currentQuantity + 1);
      this.cartItemsSubject.next(cart);
    }
  }
  
  public removeFromCart(productId: number): void {
    const cart = new Map(this.cartItemsSubject.value);
    const currentQuantity = cart.get(productId) || 0;
    
    if (currentQuantity > 0) {
      if (currentQuantity === 1) {
        cart.delete(productId);
      } else {
        cart.set(productId, currentQuantity - 1);
      }
      this.cartItemsSubject.next(cart);
    }
  }
  
  public getCartQuantity(productId: number): number {
    return this.cartItemsSubject.value.get(productId) || 0;
  }
  
  public clearCart(): void {
    this.cartItemsSubject.next(new Map());
  }
  
  public getCartItemCount(): number {
    let count = 0;
    this.cartItemsSubject.value.forEach(quantity => {
      count += quantity;
    });
    return count;
  }
  
  public getCartTotal(products: Product[]): number {
    let total = 0;
    this.cartItemsSubject.value.forEach((quantity, productId) => {
      const product = products.find(p => p.productId === productId);
      if (product) {
        total += product.unitPrice * quantity;
      }
    });
    return total;
  }
}
