import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderRequest, OrderResponse } from '../models/order.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private baseUrl = environment.apiUrls.orders || '/api/orders';

  constructor(private http: HttpClient) {}

  placeOrder(request: OrderRequest): Observable<string> {
    return this.http.post<string>(this.baseUrl, request);
  }

  getOrdersByClientId(clientId: number): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/client/${clientId}`);
  }
}
