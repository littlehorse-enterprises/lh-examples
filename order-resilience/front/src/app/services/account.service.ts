import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserAccount } from '../models/user-account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  constructor(private http: HttpClient) {}

  getAccounts(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(environment.apiUrls.customers);
  }
}
