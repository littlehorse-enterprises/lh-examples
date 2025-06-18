import { Injectable, signal, WritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../models/product.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = environment.apiUrls.products;

  products: WritableSignal<Product[]> = signal<Product[]>([]);

  constructor(private http: HttpClient) { 
    this.loadProducts();
  }


  loadProducts(): void {
    this.http.get<Product[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.products.set(data);
      },
      error: (error) => {
        console.error('Error loading products', error);
      }
    });
  }

}
