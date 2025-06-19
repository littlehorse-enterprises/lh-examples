import { Inject, Injectable, signal, WritableSignal, ɵunwrapWritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Coupon } from '../models/coupon.model';

@Injectable({
    providedIn: 'root'
})
export class CouponService {

    coupons: WritableSignal<Coupon[]> = signal<Coupon[]>([]);
    newCoupons: WritableSignal<Coupon[]> = signal<Coupon[]>([]);

    private baseUrl = environment.apiUrls.coupons || '/api/coupons';

    constructor(private http: HttpClient) { }

    getCouponsByClientId(clientId: number): Observable<Coupon[]> {
        return this.http.get<Coupon[]>(`${this.baseUrl}/client/${clientId}`);
    }
    loadCoupons(clientId: number): void {
        this.getCouponsByClientId(clientId).subscribe({
            next: (data) => {
                this.coupons.set(data);
                this.newCoupons.set(data.filter(coupon => !coupon.redeemed));
            },
            error: (error) => {
                console.error('Error loading coupons', error);
                this.coupons.set([]);
                this.newCoupons.set([]);
            }
        });
    }
    getCouponByCode(code: string): Coupon | undefined {
        return this.coupons().find(coupon => coupon.code === code);
    }
}
