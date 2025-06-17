import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ProductService } from './services/product.service';
import { ShopService } from './services/shop.service';
import { TopBarService } from '../shared/top-bar.service';
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
import { TopBarComponent } from '../shared/top-bar.component';

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
    MatSnackBarModule,
    TopBarComponent
  ],
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.scss']
})
export class ShopComponent implements OnInit {
  products: Product[] = [];
  loading = true;
  error = false;
  errorMessage = '';
  clientId: number = 1; // Default client ID, can be changed later if needed
  isProcessing = false;
  orderSuccess = false;
  orderError = false;

  constructor(
    private productService: ProductService,
    private shopService: ShopService,
    private topBarService: TopBarService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    
    // Subscribe to cart changes to update the top bar
    this.shopService.cartItems$.subscribe(cart => {
      const count = this.getCartItemCount();
      this.topBarService.updateCartCount(count);
    });
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
    this.shopService.addToCart(product);
  }

  removeFromCart(productId: number): void {
    this.shopService.removeFromCart(productId);
  }

  getCartQuantity(productId: number): number {
    return this.shopService.getCartQuantity(productId);
  }

  getCartTotal(): number {
    return this.shopService.getCartTotal(this.products);
  }

  getCartItemCount(): number {
    return this.shopService.getCartItemCount();
  }

  checkout(): void {
    const cart = this.shopService.getCart();
    if (cart.size === 0) return;
    
    this.isProcessing = true;
    this.orderSuccess = false;
    this.orderError = false;

    const productItems: ProductStockItem[] = [];
    cart.forEach((quantity, productId) => {
      productItems.push({
        productId,
        quantity
      });
    });
    
    // Add checkout logic here
    // For demonstration, we'll just simulate a successful order
    setTimeout(() => {
      this.isProcessing = false;
      this.orderSuccess = true;
      this.shopService.clearCart();
    }, 1500);
  }

  isProductInCart(productId: number): boolean {
    return this.shopService.getCartQuantity(productId) > 0;
  }
}
