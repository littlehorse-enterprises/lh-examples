import { computed, Inject, Injectable, signal, WritableSignal, ɵunwrapWritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Coupon } from '../models/coupon.model';
import { UserService } from './user.service';

@Injectable({
    providedIn: 'root'
})
export class CouponService {

    coupons: WritableSignal<Coupon[]> = signal<Coupon[]>([]);
    couponCount = computed(() => this.coupons().length);

    private baseUrl = environment.apiUrls.coupons || '/api/coupons';

    constructor(private http: HttpClient, private userService: UserService) {
        this.getCouponsEverySecond();
    }

    getCouponsByClientId(clientId: number): Observable<Coupon[]> {
        return this.http.get<Coupon[]>(`${this.baseUrl}/client/${clientId}`);
    }
    loadCoupons(clientId: number): void {
        this.getCouponsByClientId(clientId).subscribe({
            next: (data) => {
                this
                this.coupons.set(data);
            },
            error: (error) => {
                console.error('Error loading coupons', error);
                this.coupons.set([]);
            }
        });
    }
    getCouponByCode(code: string): Coupon | undefined {
        return this.coupons().find(coupon => coupon.code === code);
    }
    getCouponsEverySecond(): void {
        setInterval(() => {
            const user = this.userService.getSelectedUser();
            if (user) {
                this.loadCoupons(user.id);
            }
        }, 4000);
    }
}
