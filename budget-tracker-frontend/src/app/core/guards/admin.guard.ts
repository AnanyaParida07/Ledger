import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Provides client-side UX protection; the backend remains the security boundary. */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.role() === 'ADMIN' ? true : router.createUrlTree(['/dashboard']);
};
