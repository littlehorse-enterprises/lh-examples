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
import { OrderService } from '../services/order.service';
import { OrderResponse } from '../models/order.model';
import { Product } from '../models/product.model';
import { UserService } from '../services/user.service';
import { ProductService } from '../services/product.service';
import { animate, state, style, transition, trigger } from '@angular/animations';

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
    styleUrls: ['./orders.component.scss'],
    animations: [
        trigger('detailExpand', [
            state('collapsed', style({ height: '0px', minHeight: '0' })),
            state('expanded', style({ height: '*' })),
            transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
        ]),
    ],
})
export class OrdersComponent implements OnInit {
    orders = computed(() => this.orderService.orders());
    loading = false;
    error = false;
    errorMessage = '';
    displayedColumns: string[] = ['orderId', 'status', 'total', 'creationDate', 'items'];
    columnsToDisplayWithExpand = [...this.displayedColumns, 'expand'];
    expandedOrder: OrderResponse | null = null;
    expandedElement: OrderResponse | null = null;

    constructor(
        public orderService: OrderService,
        private userService: UserService,
        private productService: ProductService,
        private router: Router
    ) {
        // productService.loadProducts();
        effect(() => {
            const user = this.userService.user();
            const products = this.productService.products();
            console.log('userService.user() changed:', this.userService.user());
            if (user && products && products.length > 0) {
                if (!this.orderService.orders() || this.orderService.orders().length === 0) {
                    this.loadOrders(user.id, products);
                }
            }
        });
        this.orderService.orders$.subscribe({
            next: (orders) => {
                this.expandedElement = orders.length > 0 ? orders[0] : null;
            }
        });
    }
    ngOnInit(): void {
        if (!this.productService.products() || this.productService.products().length === 0) {
            this.productService.loadProducts();
        }
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
        this.expandedOrder = this.expandedOrder === order ? null : order;
    }

    toggle(element: OrderResponse): void {
        this.expandedElement = this.expandedElement === element ? null : element;
    }

    isExpanded(element: OrderResponse): boolean {
        return this.expandedElement === element;
    }

    goToShop(): void {
        this.router.navigate(['/shop']);
    }
}
