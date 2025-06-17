import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { UserAccount } from '../account-select/user-account.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private selectedUserSubject: BehaviorSubject<UserAccount | null> = new BehaviorSubject<UserAccount | null>(null);
  public selectedUser$: Observable<UserAccount | null> = this.selectedUserSubject.asObservable();
  
  constructor() {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const userJson = sessionStorage.getItem('lh-selected-user');
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.selectedUserSubject.next(user);
      } catch (e) {
        console.error('Error parsing user from session storage', e);
      }
    }
  }

  public getSelectedUser(): UserAccount | null {
    return this.selectedUserSubject.value;
  }

  public setSelectedUser(user: UserAccount): void {
    sessionStorage.setItem('lh-selected-user', JSON.stringify(user));
    this.selectedUserSubject.next(user);
  }

  public clearSelectedUser(): void {
    sessionStorage.removeItem('lh-selected-user');
    this.selectedUserSubject.next(null);
  }
}
