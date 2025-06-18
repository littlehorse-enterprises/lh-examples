import { Inject, Injectable, signal, WritableSignal, ɵunwrapWritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderRequest, OrderResponse } from '../models/order.model';
import { environment } from '../../../environments/environment';
import { UserService } from '../../shared/user.service';
import { ProductService } from './product.service';
import { Product } from '../models/product.model';

@Injectable({
    providedIn: 'root'
})
export class OrderService {

    private baseUrl = environment.apiUrls.orders || '/api/orders';
    orders: WritableSignal<OrderResponse[]> = signal<OrderResponse[]>([]);

    constructor(private http: HttpClient) { }

    placeOrder(request: OrderRequest): Observable<string> {
        return this.http.post<string>(this.baseUrl, request);
    }

    getOrdersByClientId(clientId: number): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(`${this.baseUrl}/client/${clientId}`);
    }
    loadOrders(clientId: number, products: Product[]): void {
        // await this.productService.loadProducts(); // Ensure products are loaded before fetching orders
        // const user = this.userService.user();
        this.getOrdersByClientId(clientId).subscribe({
            next: (data) => {
                data.forEach(order => {
                    order.orderLines.forEach(line => {
                        const product = products.find(p => p.productId === line.productId);
                        if (product) {
                            line.product = product; // Attach product details to order lines
                        }
                    });
                });
                this.orders.set(data);
            },
            error: (error) => {
                console.error('Error loading orders', error);
                this.orders.set([]);
            }
        });
    }

}
