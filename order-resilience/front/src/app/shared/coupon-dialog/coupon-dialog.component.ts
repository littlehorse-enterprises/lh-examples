import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Coupon } from '../../models/coupon.model';

@Component({
  selector: 'app-coupon-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './coupon-dialog.component.html',
  styleUrls: ['./coupon-dialog.component.scss']
})
export class CouponDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<CouponDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { coupons: Coupon[] }
  ) { }

  ngOnInit(): void {
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  copyCouponCode(code: string): void {
    navigator.clipboard.writeText(code).then(() => {
      // Code copied successfully
    }, (err) => {
      console.error('Error copying code to clipboard: ', err);
    });
  }
}
