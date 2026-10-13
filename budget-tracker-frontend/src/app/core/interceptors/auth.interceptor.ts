import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

// Requests that must NOT carry an Authorization header - sending a stale
// token here would be pointless, since these are the endpoints that ISSUE
// the token in the first place.
const PUBLIC_PATHS = ['/auth/login', '/auth/register'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const isPublic = PUBLIC_PATHS.some((path) => req.url.includes(path));
  const token = authService.getAccessToken();

  const authorizedReq = !isPublic && token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authorizedReq).pipe(
    catchError((error) => {
      // Access token expired or invalid - drop the stale session and send
      // the person back to login rather than showing a broken screen.
      if (error.status === 401 && !isPublic) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
