import { Component, computed, effect, inject, Inject, OnInit, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { Product } from '../../models/product.model';
import { Cart, CartItem } from '../../models/cart-item.model';
import { ShopService } from '../../services/shop.service';
import { OrderLineRequest, OrderRequest } from '../../models/order.model';
import { OrderService } from '../../services/order.service';
import { UserService } from '../../services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ProductService } from '../../services/product.service';
import { TopBarComponent } from '../../shared/top-bar.component';


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
        MatTooltipModule,
        MatProgressBarModule,
        MatCardModule,
        TopBarComponent
    ],
    templateUrl: './cart-dialog.component.html',
    styleUrls: ['./cart-dialog.component.scss'],

})
export class CartDialogComponent implements OnInit {
    shopService = inject(ShopService);

    cartItems: Signal<CartItem[]> = computed(() => this.shopService.cartItems());
    cart: Signal<Cart> = computed(() => this.shopService.cart());

    loading = false;
    errorMessage = '';
    clientId: number = 1; // Default client ID, can be changed later if needed
    isProcessing = false;
    orderSuccess = false;
    orderError = false;


    constructor(
        private orderService: OrderService,
        private userService: UserService,
        private productService: ProductService,
        private snackBar: MatSnackBar,
        private router: Router
    ) {
    }

    ngOnInit(): void {

    }


    increaseQuantity(item: CartItem): void {
        this.shopService.addToCart(item.product);
    }

    decreaseQuantity(item: CartItem): void {
        this.shopService.removeFromCart(item.product.productId);
    }

    removeItem(item: CartItem): void {
        this.shopService.deleteFromCart(item.product.productId);
    }

    applyDiscountCode(item: CartItem): void {

    }

    removeDiscount(item: CartItem): void {
        this.shopService.clearDiscountCode(item.product.productId);
    }


    placeOrder(): void {
        if (this.shopService.itemCount() === 0) return;

        this.isProcessing = true;
        this.orderSuccess = false;
        this.orderError = false;

        const user = this.userService.getSelectedUser();

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

        this.orderService.placeOrder(orderRequest).subscribe({
            next: (order) => {
                console.log('Order placed successfully, ID:', order.orderId);
                this.productService.loadProducts(); // Refresh products if needed
                this.orderService.loadOrders(user?.id || 1, this.productService.products());

                this.orderSuccess = true;

                this.snackBar.open('Order placed successfully! ID: ' + order.orderId, 'View Orders', {
                    duration: 5000
                }).onAction().subscribe(() => {
                    this.router.navigate(['/orders']);
                });

                setTimeout(() => {
                    this.shopService.clearCart();
                    // this.dialogRef.close();
                    this.router.navigate(['/orders']);
                }, 500);
            },
            error: (err) => {
                console.error('Error placing order:', err);
                this.isProcessing = false;
                this.snackBar.open('Failed to place order. Please try again.', 'Close', {
                    duration: 5000
                });
            }
        });
    }

    close(): void {
        this.router.navigate(['/shop']);
    }
}
