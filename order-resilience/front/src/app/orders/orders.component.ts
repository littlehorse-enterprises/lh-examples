import { Component, computed, effect, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TopBarComponent } from '../shared/top-bar.component';
import { OrderService } from '../shop/services/order.service';
import { UserService } from '../shared/user.service';
import { OrderResponse } from '../shop/models/order.model';
import { ProductService } from '../shop/services/product.service';
import { Product } from '../shop/models/product.model';

@Component({
    selector: 'app-orders',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        MatTabsModule,
        MatTableModule,
        MatSortModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatChipsModule,
        MatProgressSpinnerModule,
        MatExpansionModule,
        MatDividerModule,
        MatBadgeModule,
        MatSnackBarModule,
        TopBarComponent
    ],
    templateUrl: './orders.component.html',
    styleUrls: ['./orders.component.scss']
})
export class OrdersComponent {
    orders = computed(() => this.orderService.orders());
    loading = false;
    error = false;
    errorMessage = '';
    displayedColumns: string[] = ['orderId', 'status', 'total', 'creationDate', 'items', 'actions'];
    expandedOrder: OrderResponse | null = null;

    constructor(
        public orderService: OrderService,
        private userService: UserService,
        private productService: ProductService,
        private router: Router
    ) {
        effect(() => {
            console.log('userService.user() changed:', this.userService.user());
            const user = this.userService.user();
            const products = this.productService.products();
            if (user && products && products.length > 0) {
                if (!this.orderService.orders() || this.orderService.orders().length === 0) {
                    this.loadOrders(user.id, products);
                }
            }
        });
    }



    loadOrders(userId: number, products: Product[]): void {
        this.orderService.loadOrders(userId, products);
    }

    getOrderStatusClass(status: string): string {
        switch (status.toLowerCase()) {
            case 'completed':
                return 'status-completed';
            case 'processing':
                return 'status-processing';
            case 'shipped':
                return 'status-shipped';
            case 'cancelled':
                return 'status-cancelled';
            case 'pending':
                return 'status-pending';
            default:
                return '';
        }
    }

    getOrderDate(dateStr: string | Date): string {
        const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr;
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    }

    getItemCount(order: OrderResponse): number {
        return order.orderLines.reduce((sum, line) => sum + line.quantity, 0);
    }

    toggleOrderDetails(order: OrderResponse): void {
        if (this.expandedOrder === order) {
            this.expandedOrder = null;
        } else {
            this.expandedOrder = order;
        }
    }

    goToShop(): void {
        this.router.navigate(['/shop']);
    }
}
