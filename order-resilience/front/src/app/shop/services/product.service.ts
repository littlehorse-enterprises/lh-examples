import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';
import { ProductStockItem } from '../models/product-stock-item.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = environment.apiUrls.products;

  constructor(private http: HttpClient) { }

  getProducts(): Observable<Product[]> {
    return new Observable<Product[]>(observer => {
      this.http.get<Product[]>(this.apiUrl).subscribe({
        next: (products) => {
          // Map DB fields to UI fields for backward compatibility
          const mappedProducts = products.map(product => {
            return {
              ...product,
              unitPrice: product.unitPrice,
              availableStock: product.quantity
            };
          });
          observer.next(mappedProducts);
          observer.complete();
        },
        error: (err) => {
          observer.error(err);
        }
      });
    });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }


}
