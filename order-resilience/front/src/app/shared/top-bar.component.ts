import { Component, OnInit, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { Router } from '@angular/router';
import { UserAccount } from '../models/user-account.model';
import { CartDialogComponent } from '../shop/cart-dialog/cart-dialog.component';
import { UserService } from '../services/user.service';
import { ShopService } from '../services/shop.service';
import { ProductService } from '../services/product.service';

@Component({
    selector: 'app-top-bar',
    standalone: true,
    imports: [
        CommonModule,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        MatBadgeModule,
        MatDialogModule,
        MatMenuModule,
        MatDividerModule,
    ],
    templateUrl: './top-bar.component.html',
    styleUrls: ['./top-bar.component.scss']
})
export class TopBarComponent implements OnInit {


    dialog = inject(MatDialog);
    shopService = inject(ShopService);
    productService = inject(ProductService);
    userService = inject(UserService);
    selectedUser= computed(() => this.userService.getSelectedUser() as UserAccount);
    cartItemCount = computed(() => this.shopService.itemCount());

    constructor(
        private router: Router
    ) { }

    ngOnInit(): void {

    }

    viewCart(): void {

        const dialogRef = this.dialog.open(CartDialogComponent, {
            width: '900px',
            disableClose: true,
            data: {
                products: this.productService.products(),
                cartItems: this.shopService.getCart()
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result === 'checkout') {
                // Redirect to the shop page and trigger checkout
                this.router.navigate(['/shop'], { queryParams: { checkout: true } });
            }
        });
    }

    goToShop(): void {
        this.router.navigate(['/shop']);
    }

    goToOrders(): void {
        this.router.navigate(['/orders']);
    }

  

    logout(): void {
        this.shopService.clearCart();
        this.userService.clearSelectedUser();
        this.router.navigate(['/']);
    }
}
