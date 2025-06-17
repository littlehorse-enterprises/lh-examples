import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserAccount } from './user-account.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AccountService {
  constructor(private http: HttpClient) {}

  getAccounts(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(environment.apiUrls.customers);
  }
}
