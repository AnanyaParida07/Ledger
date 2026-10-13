import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { AuthLayoutComponent } from '../auth-layout/auth-layout.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AuthLayoutComponent],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  readonly form;

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService.login(this.form.getRawValue() as { email: string; password: string }).subscribe({
      next: (response) => {
        if (response.mustChangePassword) {
          this.router.navigate(['/change-password']);
          return;
        }
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/analytics';
        this.router.navigateByUrl(returnUrl);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Incorrect email or password.');
      },
    });
  }
}
