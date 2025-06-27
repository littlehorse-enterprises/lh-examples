import { computed, Injectable, Signal, signal, WritableSignal, effect, inject } from '@angular/core';
import { Cart, CartItem } from '../models/cart-item.model';
import { Product } from '../models/product.model';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ProductService } from './product.service';
import { CouponService } from './coupon.service';
import { MessageService } from './message.service';
import { UserService } from './user.service';



@Injectable({
    providedIn: 'root'
})
export class ShopService {
    dialog = inject(MatDialog);
    productService = inject(ProductService);
    router = inject(Router);
    couponService = inject(CouponService);
    messageService: MessageService = inject(MessageService);
    userService = inject(UserService);

    cartItems: WritableSignal<CartItem[]> = signal<CartItem[]>([]);
    itemCount: Signal<number> = computed(() => this.cartItems().reduce((count, item) => count + item.quantity, 0));

    cart: Signal<Cart> = computed(() => {
        const items = this.cartItems();
        const subtotal = items.reduce((total, item) => {
            const product = item.product;
            return total + (product ? product.unitPrice * item.quantity : 0);
        }, 0);
        const discount = items.reduce((total, item) => {
            if (item.discountPercentage) {
                const product = item.product;
                return total + (product ? product.unitPrice * item.quantity * (item.discountPercentage / 100) : 0);
            }
            return total;
        }, 0);
        const total = subtotal - discount;
        return {
            items: items,
            subtotal: subtotal,
            discount: discount,
            total: total
        };
    });
    constructor() {
        effect(() => {
            console.log('Cart items changed:', this.cartItems());
        });
    }

    getCart(): CartItem[] {
        return this.cartItems();
    }

    addToCart(product: Product): void {
        const currentCart = this.cartItems();
        const currentItem = currentCart.find(item => item.productId === product.productId);
        const currentQuantity = currentItem?.quantity ?? 0;

        if (currentQuantity < product.quantity) {
            const newCart = currentItem
                ? currentCart.map(item =>
                    item.productId === product.productId
                        ? { ...item, quantity: item.quantity + 1 }
                        : item
                )
                : [...currentCart, { productId: product.productId, quantity: 1, product: product, discountPercentage: 0 }];

            this.cartItems.set(newCart);
        }
    }

    removeFromCart(productId: number): void {
        const currentCart = this.cartItems();
        const currentItem = currentCart.find(item => item.productId === productId);

        if (!currentItem) return;

        let newCart: CartItem[];

        if (currentItem.quantity === 1) {
            newCart = currentCart.filter(item => item.productId !== productId);
        } else {
            newCart = currentCart.map(item =>
                item.productId === productId
                    ? { ...item, quantity: item.quantity - 1 }
                    : item
            );
        }

        this.cartItems.set(newCart);
    }
    deleteFromCart(productId: number): void {
        const newCart = this.cartItems().filter(item => item.productId !== productId);
        this.cartItems.set(newCart);
    }



    getCartQuantity(productId: number): number {
        const item = this.cartItems().find(item => item.productId === productId);
        return item ? item.quantity : 0;
    }

    setDiscountCode(productId: number, code: string): void {
        if (!code) {
            return;
        }
        const coupon = this.couponService.getCouponByCode(code);
        if (!coupon) {
            this.messageService.error(`Coupon with code ${code} does not exist.`);
            return;
        }
        if (coupon.clientId && coupon.clientId !== this.userService.user()?.id) { 
            this.messageService.error(`Coupon with code ${code} is not valid for this client.`);
            return;
        }
        if (coupon.productId && coupon.productId !== productId) {
            this.messageService.error(`Coupon with code ${code} is not valid for product ID ${productId}.`);
            return;
        }
        const newCart = this.cartItems().map(item =>
            item.productId === productId
                ? {
                    ...item,
                    discountCode: code,
                    discountPercentage: coupon.discountPercentage
                }
                : item
        );
        this.cartItems.set(newCart);
    }

    clearDiscountCode(productId: number): void {
        const newCart = this.cartItems().map(item =>
            item.productId === productId
                ? {
                    ...item,
                    discountCode: '',
                    discountPercentage: 0
                }
                : item
        );
        this.cartItems.set(newCart);
    }

    clearCart(): void {
        this.cartItems.set([]);
    }

    getAppliedDiscountCodes(): string[] {
        return this.cartItems()
            .filter(item => item.discountPercentage && item.discountCode)
            .map(item => item.discountCode!)
    }

    getCartTotal(products: Product[]): number {
        return this.cartItems().reduce((total, item) => {
            const product = products.find(p => p.productId === item.productId);
            return product ? total + product.unitPrice * item.quantity : total;
        }, 0);
    }

    getDiscountAmount(products: Product[]): number {
        return this.cartItems().reduce((discount, item) => {
            if (item.discountPercentage) {
                const product = products.find(p => p.productId === item.productId);
                if (product) {
                    discount += product.unitPrice * item.quantity * 0.1;
                }
            }
            return discount;
        }, 0);
    }

    getFinalTotal(products: Product[]): number {
        return this.getCartTotal(products) - this.getDiscountAmount(products);
    }
    viewCart(): void {
        this.router.navigate(['/checkout']);
    }
}
