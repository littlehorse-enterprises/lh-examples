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
import { Product } from '../models/product.model';
import { ShopService } from '../services/shop.service';
import { CartItem } from '../../models/cart-item.model';

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
        MatTooltipModule
    ],
    templateUrl: './cart-dialog.component.html',
    styleUrls: ['./cart-dialog.component.scss'],

})
export class CartDialogComponent implements OnInit {
    shopService = inject(ShopService);
    cartItems: Signal<CartItem[]> = computed(() => this.shopService.cartItems());
    subtotal: number = 0;
    discount: number = 0;
    total: number = 0;


    constructor(
        public dialogRef: MatDialogRef<CartDialogComponent>,
    ) {
    }

    ngOnInit(): void {
        this.calculateTotals();

    }


    increaseQuantity(item: CartItem): void {
        this.shopService.addToCart(item.product);
        this.calculateTotals();
    }

    decreaseQuantity(item: CartItem): void {
        this.shopService.removeFromCart(item.product.productId);
        this.calculateTotals();
    }

    removeItem(item: CartItem): void {
        // Remove all instances of this product from the cart
        this.shopService.deleteFromCart(item.product.productId);

        this.calculateTotals();

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
        item.discountApplied = false;
        item.discountCode = '';
        this.shopService.clearDiscountCode(item.product.productId);
        this.calculateTotals();
    }

    calculateTotals(): void {
        this.subtotal = 0;
        this.discount = 0;

        for (const item of this.cartItems()) {
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
        // Ensure any pending discount codes are applied before proceeding
        this.cartItems().forEach(item => {
            if (item.discountCode && !item.discountApplied) {
                this.applyDiscountCode(item);
            }
        });

        this.dialogRef.close('checkout');
    }

    close(): void {
        this.dialogRef.close();
    }
}
