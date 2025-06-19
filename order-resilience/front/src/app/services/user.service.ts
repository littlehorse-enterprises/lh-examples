import { Injectable, signal, WritableSignal } from '@angular/core';
import { UserAccount } from '../models/user-account.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    user: WritableSignal<UserAccount | null> = signal<UserAccount | null>(null);

    constructor() {
        this.loadUserFromStorage();
    }

    private loadUserFromStorage(): void {
        const userJson = localStorage.getItem('lh-selected-user');
        if (userJson) {
            try {
                const user = JSON.parse(userJson);
                this.user.set(user);
            } catch (e) {
                console.error('Error parsing user from local storage', e);
            }
        }
    }

    getSelectedUser(): UserAccount | null {
        return this.user();
    }

    setSelectedUser(user: UserAccount): void {
        localStorage.setItem('lh-selected-user', JSON.stringify(user));
        this.user.set(user);
    }

    clearSelectedUser(): void {
        localStorage.removeItem('lh-selected-user');
        this.user.set(null);
    }
}
