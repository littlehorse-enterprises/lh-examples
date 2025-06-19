import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UserAccount } from '../models/user-account.model';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { AccountService } from '../services/account.service';
import { UserService } from '../services/user.service';

@Component({
    selector: 'app-account-select',
    templateUrl: './account-select.component.html',
    styleUrl: './account-select.component.scss',
    standalone: true,
    imports: [MatCardModule, MatButtonModule],
})
export class AccountSelectComponent {
    private accountService = inject(AccountService);
    private userService = inject(UserService);
    private router = inject(Router);
    accounts: UserAccount[] = [];
    loading = true;

    ngOnInit() {
        this.accountService.getAccounts().subscribe(users => {
            // Add default profile images if they don't have one
            this.accounts = users.map(user => {
                if (!user.profileImage) {
                    return {
                        ...user,
                        profileImage: '/assets/images/default-user.png'
                    };
                }
                return user;
            });
            this.loading = false;
        });
    }

    selectAccount(account: UserAccount) {
        this.userService.setSelectedUser(account);
        this.router.navigate(['/shop']); // Redirect to shopping app
    }
}
