import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { UserService } from '../shared/user.service';
import { TopBarService } from './top-bar.service';
import { UserAccount } from '../account-select/user-account.model';
import { CartDialogComponent } from '../shop/cart-dialog/cart-dialog.component';
import { ShopService } from '../shop/services/shop.service';
import { ProductService } from '../shop/services/product.service';

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule,
    MatTooltipModule,
    MatDialogModule
  ],
  templateUrl: './top-bar.component.html',
  styleUrls: ['./top-bar.component.scss']
})
export class TopBarComponent implements OnInit {
  selectedUser: UserAccount | null = null;
  cartItemCount: number = 0;
  
  dialog = inject(MatDialog);
  shopService = inject(ShopService);
  productService = inject(ProductService);

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
    if (this.cartItemCount === 0) {
      // Don't open the dialog if the cart is empty
      return;
    }

    // Fetch the latest products to ensure we have current data
    this.productService.getProducts().subscribe(products => {
      const dialogRef = this.dialog.open(CartDialogComponent, {
        width: '800px',
        data: {
          products: products,
          cartItems: this.shopService.getCart()
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result === 'checkout') {
          // Navigate to checkout or trigger checkout process
          this.router.navigate(['/shop']);
        }
      });
    });
  }
  
  changeUser(): void {
    this.router.navigate(['/']);
  }
}
