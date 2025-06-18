import { Component, inject, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProductService } from './services/product.service';
import { ShopService } from './services/shop.service';
import { OrderService } from './services/order.service';
import { UserService } from '../shared/user.service';
import { Product } from './models/product.model';
import { OrderRequest, OrderLineRequest } from './models/order.model';
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
    private orderService: OrderService,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Handle checkout parameter from URL
    this.route.queryParams.subscribe(params => {
      if (params['checkout'] === 'true' && this.shopService.itemCount() > 0) {
        setTimeout(() => this.checkout(), 500); // Give a small delay to ensure everything is loaded
      }
    });
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

  // getCartTotal(): number {
  //   // return this.shopService.getCartTotal(this.products);
  // }


  checkout(): void {
    if (this.shopService.itemCount() === 0) return;

    this.isProcessing = true;
    this.orderSuccess = false;
    this.orderError = false;

    // Get the selected user ID
    const user = this.userService.getSelectedUser();

    // Create order request
    const orderLines: OrderLineRequest[] = [];
    this.shopService.cartItems().forEach((item) => {
      orderLines.push({
        productId: item.productId,
        quantity: item.quantity
      });
    });

    const orderRequest: OrderRequest = {
      clientId: user?.id,
      orderLines: orderLines,
      discountCodes: this.shopService.getAppliedDiscountCodes()
    };

    // Send order to the backend
    this.orderService.placeOrder(orderRequest).subscribe({
      next: (orderId) => {
        console.log('Order placed successfully, ID:', orderId);
        this.isProcessing = false;
        this.orderSuccess = true;
        this.shopService.clearCart();

        // Show success message
        this.snackBar.open('Order placed successfully!', 'View Orders', {
          duration: 5000
        }).onAction().subscribe(() => {
          this.router.navigate(['/orders']);
        });

        // Redirect to orders page after a brief delay
        setTimeout(() => {
          this.router.navigate(['/orders']);
        }, 2000);
      },
      error: (err) => {
        console.error('Error placing order:', err);
        this.isProcessing = false;
        this.orderError = true;
        this.snackBar.open('Failed to place order. Please try again.', 'Close', {
          duration: 5000
        });
      }
    });
  }

  isProductInCart(productId: number): boolean {
    return this.shopService.getCartQuantity(productId) > 0;
  }
}
