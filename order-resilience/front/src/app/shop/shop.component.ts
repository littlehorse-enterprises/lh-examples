import { Component, inject, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from '../models/product.model';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TopBarComponent } from '../shared/top-bar.component';
import { ProductService } from '../services/product.service';
import { ShopService } from '../services/shop.service';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-shop',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDividerModule,
    MatSnackBarModule,
    MatProgressBarModule,
    TopBarComponent
  ],
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.scss']
})
export class ShopComponent implements OnInit {

  productService = inject(ProductService);
  shopService = inject(ShopService);
  loading = false;
  error = false;
  errorMessage = '';
  clientId: number = 1; // Default client ID, can be changed later if needed
  isProcessing = false;
  orderSuccess = false;
  orderError = false;

  constructor(

  ) { }

  ngOnInit(): void {
    this.productService.loadProducts();

  }

  loadProducts(): void {
    this.productService.loadProducts();
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
    return this.shopService.cart().total;
  }

  isProductInCart(productId: number): boolean {
    return this.shopService.getCartQuantity(productId) > 0;
  }
  viewCart(): void {
    this.shopService.viewCart();
  }
}
