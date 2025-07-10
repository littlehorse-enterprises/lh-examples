import { Injectable, signal, WritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderRequest, OrderResponse } from '../models/order.model';
import { environment } from '../../environments/environment';
import { Product } from '../models/product.model';
import { toObservable } from '@angular/core/rxjs-interop';

@Injectable({
    providedIn: 'root'
})
export class OrderService {

    orders: WritableSignal<OrderResponse[]> = signal<OrderResponse[]>([]);
    orders$ = toObservable(this.orders);

    private baseUrl = environment.apiUrls.orders || '/api/orders';

    constructor(private http: HttpClient) { }

    placeOrder(request: OrderRequest): Observable<OrderResponse> {
        return this.http.post<OrderResponse>(this.baseUrl, request);
    }

    getOrdersByClientId(clientId: number): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(`${this.baseUrl}/client/${clientId}`);
    }
    loadOrders(clientId: number, products: Product[]): void {
        this.getOrdersByClientId(clientId).subscribe({
            next: (data) => {
                data.sort((a, b) => b.orderId - a.orderId).forEach(order => {
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
    addOrder(order: OrderResponse, products: Product[]): void {
        const currentOrders = this.orders();
        order.orderLines.forEach(line => {
            const product = products.find(p => p.productId === line.productId);
            if (product) {
                line.product = product; // Attach product details to order lines
            }
        });
        this.orders.set([...currentOrders, order]);
    }

}
