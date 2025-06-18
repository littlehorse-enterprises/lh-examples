import { computed, Injectable, Signal, signal, WritableSignal, effect } from '@angular/core';
import { Product } from '../models/product.model';
import { CartItem } from '../../models/cart-item.model';



@Injectable({
    providedIn: 'root'
})
export class ShopService {
    cartItems: WritableSignal<CartItem[]> = signal<CartItem[]>([]);
    itemCount: Signal<number> = computed(() => this.cartItems().reduce((count, item) => count + item.quantity, 0));

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
                : [...currentCart, { productId: product.productId, quantity: 1, product: product }];

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
        const newCart = this.cartItems().map(item =>
            item.productId === productId
                ? {
                    ...item,
                    discountCode: code,
                    discountApplied: code.trim() !== ''
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
                    discountApplied: false
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
            .filter(item => item.discountApplied && item.discountCode)
            .map(item => item.discountCode!) // `!` porque ya filtramos los falsy
    }

    getCartTotal(products: Product[]): number {
        return this.cartItems().reduce((total, item) => {
            const product = products.find(p => p.productId === item.productId);
            return product ? total + product.unitPrice * item.quantity : total;
        }, 0);
    }

    getDiscountAmount(products: Product[]): number {
        return this.cartItems().reduce((discount, item) => {
            if (item.discountApplied) {
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
}
