import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Keeps first-time users in the password-change flow until it is complete. */
export const passwordChangeGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.mustChangePassword() ? router.createUrlTree(['/change-password']) : true;
};
