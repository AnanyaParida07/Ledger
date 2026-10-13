import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.models';

const ACCESS_TOKEN_KEY = 'ledger_access_token';
const REFRESH_TOKEN_KEY = 'ledger_refresh_token';
const TENANT_ID_KEY = 'ledger_tenant_id';
const ROLE_KEY = 'ledger_role';
const MUST_CHANGE_PASSWORD_KEY = 'ledger_must_change_password';

/**
 * Holds auth state as signals so components react to login/logout without
 * manual subscriptions. Tokens are persisted to localStorage for simplicity -
 * fine for a portfolio project, but note this is readable by any script on
 * the page (XSS risk). A production app would prefer an httpOnly cookie
 * issued by the backend instead.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly accessTokenSignal = signal<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));
  private readonly roleSignal = signal<string | null>(localStorage.getItem(ROLE_KEY));
  private readonly mustChangePasswordSignal = signal(localStorage.getItem(MUST_CHANGE_PASSWORD_KEY) === 'true');

  readonly isAuthenticated = computed(() => this.accessTokenSignal() !== null);
  readonly role = this.roleSignal.asReadonly();
  readonly mustChangePassword = this.mustChangePasswordSignal.asReadonly();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/register`, request)
      .pipe(tap((response) => this.storeSession(response)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(tap((response) => this.storeSession(response)));
  }

  changePassword(newPassword: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/auth/change-password`, { newPassword }).pipe(
      tap(() => {
        localStorage.setItem(MUST_CHANGE_PASSWORD_KEY, 'false');
        this.mustChangePasswordSignal.set(false);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(TENANT_ID_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(MUST_CHANGE_PASSWORD_KEY);
    this.accessTokenSignal.set(null);
    this.roleSignal.set(null);
    this.mustChangePasswordSignal.set(false);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return this.accessTokenSignal();
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(TENANT_ID_KEY, response.tenantId);
    localStorage.setItem(ROLE_KEY, response.role);
    localStorage.setItem(MUST_CHANGE_PASSWORD_KEY, String(response.mustChangePassword));
    this.accessTokenSignal.set(response.accessToken);
    this.roleSignal.set(response.role);
    this.mustChangePasswordSignal.set(response.mustChangePassword);
  }
}
