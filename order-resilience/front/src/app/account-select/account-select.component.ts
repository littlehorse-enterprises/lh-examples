import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AccountService } from './account.service';
import { UserAccount } from './user-account.model';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

@Component({
    selector: 'app-account-select',
    templateUrl: './account-select.component.html',
    styleUrl: './account-select.component.scss',
    standalone: true,
    imports: [MatCardModule, MatButtonModule],
})
export class AccountSelectComponent {
    private accountService = inject(AccountService);
    private router = inject(Router);
    accounts: UserAccount[] = [];
    loading = true;

    ngOnInit() {
        this.accountService.getAccounts().subscribe(users => {
            this.accounts = users;
            this.loading = false;
        });
    }

    selectAccount(account: UserAccount) {
        sessionStorage.setItem('lh-selected-user', JSON.stringify(account));
        this.router.navigate(['/shop']); // Redirect to shopping app
    }
}
