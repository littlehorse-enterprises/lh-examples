import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { UserService } from '../shared/user.service';
import { TopBarService } from './top-bar.service';
import { UserAccount } from '../account-select/user-account.model';

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule,
    MatTooltipModule
  ],
  templateUrl: './top-bar.component.html',
  styleUrls: ['./top-bar.component.scss']
})
export class TopBarComponent implements OnInit {
  selectedUser: UserAccount | null = null;
  cartItemCount: number = 0;
  
  constructor(
    private userService: UserService,
    private topBarService: TopBarService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.userService.selectedUser$.subscribe(user => {
      this.selectedUser = user;
    });
    
    this.topBarService.cartItemCount$.subscribe(count => {
      this.cartItemCount = count;
    });
  }
  
  viewCart(): void {
    // For now, this just redirects to the shop page
    // In the future, it could open a cart dialog or navigate to a cart page
    this.router.navigate(['/shop']);
  }
  
  changeUser(): void {
    this.router.navigate(['/']);
  }
}
