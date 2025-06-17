import { Component, OnInit } from '@angular/core';
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
export class OrdersComponent implements OnInit {
  orders: OrderResponse[] = [];
  loading = false;
  error = false;
  errorMessage = '';
  displayedColumns: string[] = ['id', 'status', 'totalAmount', 'createdAt', 'items', 'actions'];
  expandedOrder: OrderResponse | null = null;
  
  constructor(
    private orderService: OrderService,
    private userService: UserService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.loadOrders();
  }
  
  loadOrders(): void {
    const user = this.userService.getSelectedUser();
    if (!user) {
      this.error = true;
      this.errorMessage = 'No user selected. Please select a user to view orders.';
      return;
    }
    
    this.loading = true;
    this.error = false;
    
    this.orderService.getOrdersByClientId(user.id).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching orders:', err);
        this.loading = false;
        this.error = true;
        this.errorMessage = 'Failed to load orders. Please try again later.';
      }
    });
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
  
  getOrderDate(dateStr: string): string {
    const date = new Date(dateStr);
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
