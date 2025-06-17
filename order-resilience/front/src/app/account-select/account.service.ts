import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserAccount } from './user-account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  constructor(private http: HttpClient) {}

  getAccounts(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>('http://localhost:8081/api/customers');
  }
}
