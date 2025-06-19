import { Inject, Injectable, signal, WritableSignal, ɵunwrapWritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderRequest, OrderResponse } from '../models/order.model';
import { environment } from '../../environments/environment';
import { Product } from '../models/product.model';

@Injectable({
    providedIn: 'root'
})
export class OrderService {

    orders: WritableSignal<OrderResponse[]> = signal<OrderResponse[]>([]);

    private baseUrl = environment.apiUrls.orders || '/api/orders';

    constructor(private http: HttpClient) { }

    placeOrder(request: OrderRequest): Observable<string> {
        return this.http.post<string>(this.baseUrl, request);
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

}
