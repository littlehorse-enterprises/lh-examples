import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ProductService } from './services/product.service';
import { Product } from './models/product.model';
import { ProductStockItem } from './models/product-stock-item.model';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-shop',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    HttpClientModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.scss']
})
export class ShopComponent implements OnInit {
  products: Product[] = [];
  loading = true;
  error = false;
  errorMessage = '';
  cart: Map<number, number> = new Map<number, number>(); // productId -> quantity
  clientId: number = 1; // Default client ID, can be changed later if needed
  isProcessing = false;
  orderSuccess = false;
  orderError = false;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.error = false;
    
    this.productService.getProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching products:', err);
        this.error = true;
        this.loading = false;
        this.errorMessage = 'Failed to load products. Please try again later.';
      }
    });
  }

  addToCart(product: Product): void {
    if (product.quantity <= 0) return;
    
    const currentQuantity = this.cart.get(product.productId) || 0;
    if (currentQuantity < product.quantity) {
      this.cart.set(product.productId, currentQuantity + 1);
    }
  }

  removeFromCart(productId: number): void {
    const currentQuantity = this.cart.get(productId) || 0;
    if (currentQuantity > 0) {
      if (currentQuantity === 1) {
        this.cart.delete(productId);
      } else {
        this.cart.set(productId, currentQuantity - 1);
      }
    }
  }

  getCartQuantity(productId: number): number {
    return this.cart.get(productId) || 0;
  }

  getCartTotal(): number {
    let total = 0;
    this.cart.forEach((quantity, productId) => {
      const product = this.products.find(p => p.productId === productId);
      if (product) {
        total += product.unitPrice * quantity;
      }
    });
    return total;
  }

  getCartItemCount(): number {
    let count = 0;
    this.cart.forEach(quantity => {
      count += quantity;
    });
    return count;
  }

  checkout(): void {
    if (this.cart.size === 0) return;
    
    this.isProcessing = true;
    this.orderSuccess = false;
    this.orderError = false;

    const productItems: ProductStockItem[] = [];
    this.cart.forEach((quantity, productId) => {
      productItems.push({
        productId,
        quantity
      });
    });
  }

  isProductInCart(productId: number): boolean {
    return this.cart.has(productId);
  }
}
