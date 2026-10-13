import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { AuthLayoutComponent } from '../auth-layout/auth-layout.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AuthLayoutComponent],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  readonly form;

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      companyName: ['', [Validators.required]],
      fullName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService
      .register(
        this.form.getRawValue() as {
          companyName: string;
          fullName: string;
          email: string;
          password: string;
        }
      )
      .subscribe({
        next: () => this.router.navigateByUrl('/dashboard'),
        error: (err: HttpErrorResponse) => {
          this.isSubmitting.set(false);
          const serverMessage =
            typeof err.error?.message === 'string' ? err.error.message : '';

          if (err.status === 0) {
            this.errorMessage.set('Cannot reach the server. Make sure the backend is running and try again.');
          } else if (/email already in use/i.test(serverMessage)) {
            this.errorMessage.set('That email is already registered.');
          } else if (serverMessage) {
            this.errorMessage.set(serverMessage);
          } else {
            this.errorMessage.set('Unable to create your workspace. Please check your details and try again.');
          }
        },
      });
  }
}
