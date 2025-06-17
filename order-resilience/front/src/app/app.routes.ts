import { Routes } from '@angular/router';
import { AccountSelectComponent } from './account-select/account-select.component';

export const routes: Routes = [
  { path: '', component: AccountSelectComponent },
  { path: 'shop', loadComponent: () => import('./shop/shop.component').then(m => m.ShopComponent) },
];
