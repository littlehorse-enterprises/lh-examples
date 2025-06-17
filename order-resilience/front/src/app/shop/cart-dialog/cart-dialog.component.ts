import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Product } from '../models/product.model';
import { ShopService } from '../services/shop.service';

export interface CartDialogData {
  products: Product[];
  cartItems: Map<number, number>;
}

interface CartItem {
  product: Product;
  quantity: number;
  discountCode: string;
  discountApplied: boolean;
}

@Component({
  selector: 'app-cart-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDividerModule,
    MatListModule,
    MatExpansionModule,
    MatTooltipModule
  ],
  templateUrl: './cart-dialog.component.html',
  styleUrls: ['./cart-dialog.component.scss']
})
export class CartDialogComponent implements OnInit {
  cartItems: CartItem[] = [];
  subtotal: number = 0;
  discount: number = 0;
  total: number = 0;
  
  constructor(
    public dialogRef: MatDialogRef<CartDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CartDialogData,
    private shopService: ShopService
  ) {}
  
  ngOnInit(): void {
    this.initializeCartItems();
    this.calculateTotals();
  }
  
  private initializeCartItems(): void {
    this.cartItems = [];
    this.data.cartItems.forEach((quantity, productId) => {
      const product = this.data.products.find(p => p.productId === productId);
      if (product) {
        this.cartItems.push({
          product,
          quantity,
          discountCode: '',
          discountApplied: false
        });
      }
    });
  }
  
  increaseQuantity(item: CartItem): void {
    if (item.quantity < item.product.quantity) {
      item.quantity++;
      this.shopService.addToCart(item.product);
      this.calculateTotals();
    }
  }
  
  decreaseQuantity(item: CartItem): void {
    if (item.quantity > 1) {
      item.quantity--;
      this.shopService.removeFromCart(item.product.productId);
      this.calculateTotals();
    }
  }
  
  removeItem(item: CartItem): void {
    // Remove all instances of this product from the cart
    for (let i = 0; i < item.quantity; i++) {
      this.shopService.removeFromCart(item.product.productId);
    }
    
    // Remove item from local array
    const index = this.cartItems.indexOf(item);
    if (index !== -1) {
      this.cartItems.splice(index, 1);
    }
    
    this.calculateTotals();
    
    // Close dialog if cart is empty
    if (this.cartItems.length === 0) {
      this.dialogRef.close();
    }
  }
  
  applyDiscountCode(item: CartItem): void {
    // In a real application, this would validate the discount code with a backend service
    // For now, we'll simulate applying a 10% discount if any code is entered
    if (item.discountCode.trim() !== '') {
      item.discountApplied = true;
      this.calculateTotals();
    }
  }
  
  removeDiscount(item: CartItem): void {
    item.discountApplied = false;
    item.discountCode = '';
    this.calculateTotals();
  }
  
  calculateTotals(): void {
    this.subtotal = 0;
    this.discount = 0;
    
    for (const item of this.cartItems) {
      const itemTotal = item.product.unitPrice * item.quantity;
      this.subtotal += itemTotal;
      
      if (item.discountApplied) {
        // Apply a 10% discount for demonstration purposes
        this.discount += itemTotal * 0.1;
      }
    }
    
    this.total = this.subtotal - this.discount;
  }
  
  checkout(): void {
    this.dialogRef.close('checkout');
  }
  
  close(): void {
    this.dialogRef.close();
  }
}
