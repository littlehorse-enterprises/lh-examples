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
        // Initialize coupons from local storage if available
        const storedCoupons = this.getStoredCoupons();
        if (storedCoupons.length > 0) {
            this.coupons.set(storedCoupons);
        }
    }

    getCouponsByClientId(clientId: number): Observable<Coupon[]> {
        return this.http.get<Coupon[]>(`${this.baseUrl}/client/${clientId}`);
    }
    loadCoupons(clientId: number): void {
        this.getCouponsByClientId(clientId).subscribe({
            next: (data) => {
                // Compare with stored coupons to avoid duplicates
                if (data.length === 0) {
                    this.coupons.set([]);
                    this.storeCoupons([]);
                    return;
                }
                const storedCoupons = this.getStoredCoupons();
                const newCoupons = data.filter(coupon => !storedCoupons.some(stored => stored.code === coupon.code));
                if (newCoupons.length > 0) {
                    this.coupons.set(newCoupons);
                }
                this.storeCoupons(data);
            },
            error: (error) => {
                console.error('Error loading coupons', error);
                this.coupons.set([]);
            }
        });
    }
    getCouponByCode(code: string): Coupon | undefined {
        return this.getStoredCoupons().find(coupon => coupon.code === code);
    }
    getStoredCoupons(): Coupon[] {
        const storedCoupons = localStorage.getItem('coupons-' + this.userService.getSelectedUser()?.id);
        return storedCoupons ? JSON.parse(storedCoupons) : [];
    }
    storeCoupons(coupons: Coupon[]): void {
        localStorage.setItem('coupons-' + this.userService.getSelectedUser()?.id, JSON.stringify(coupons));
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
