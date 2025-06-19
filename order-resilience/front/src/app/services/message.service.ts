import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private snackBar = inject(MatSnackBar);

  success(message: string, duration: number = 10000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['success-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'bottom'
    });
  }

  error(message: string, duration: number = 25000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['error-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'bottom'
    });
  }

  info(message: string, duration: number = 5000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['info-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'bottom'
    });
  }

  warning(message: string, duration: number = 4000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['warning-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'bottom'
    });
  }
}