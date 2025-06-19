import { Component, computed, effect, inject, Inject, OnInit, Signal } from '@angular/core';
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
import { Product } from '../../models/product.model';
import { Cart, CartItem } from '../../models/cart-item.model';
import { ShopService } from '../../services/shop.service';
import { OrderLineRequest, OrderRequest } from '../../models/order.model';
import { OrderService } from '../../services/order.service';
import { UserService } from '../../services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { MatProgressBarModule } from '@angular/material/progress-bar';

export interface CartDialogData {
    products: Product[];
    cartItems: CartItem[];
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
        MatTooltipModule,
        MatProgressBarModule
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
        public dialogRef: MatDialogRef<CartDialogComponent>,
        private orderService: OrderService,
        private userService: UserService,
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
        // Remove all instances of this product from the cart
        this.shopService.deleteFromCart(item.product.productId);


        // Close dialog if cart is empty
        if (this.cartItems().length === 0) {
            this.dialogRef.close();
        }
    }

    applyDiscountCode(item: CartItem): void {
        // In a real application, this would validate the discount code with a backend service
        // For now, we'll simulate applying a 10% discount if any code is entered
        // if (item.discountCode.trim() !== '') {
        //   item.discountApplied = true;
        //   this.shopService.setDiscountCode(item.product.productId, item.discountCode);
        //   this.calculateTotals();
        // }
    }

    removeDiscount(item: CartItem): void {
        this.shopService.clearDiscountCode(item.product.productId);
    }


    // checkout(): void {
    //     this.dialogRef.close('checkout');
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
                this.orderSuccess = true;
                this.shopService.clearCart();

                // Show success message
                this.snackBar.open('Order placed successfully!', 'View Orders', {
                    duration: 5000
                }).onAction().subscribe(() => {
                    this.router.navigate(['/orders']);
                });
                this.dialogRef.close();

                this.router.navigate(['/orders']);
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
        this.dialogRef.close();
    }
}
