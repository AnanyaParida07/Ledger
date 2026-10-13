import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { InviteUserRequest, InviteUserResponse, UserResponse } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class UserManagementService {
  constructor(private http: HttpClient) {}

  list(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${environment.apiUrl}/users`);
  }

  invite(request: InviteUserRequest): Observable<InviteUserResponse> {
    return this.http.post<InviteUserResponse>(`${environment.apiUrl}/users/invite`, request);
  }

  me(): Observable<UserResponse> { return this.http.get<UserResponse>(`${environment.apiUrl}/users/me`); }
  update(id: string, request: { fullName: string; email: string }): Observable<UserResponse> { return this.http.put<UserResponse>(`${environment.apiUrl}/users/${id}`, request); }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${environment.apiUrl}/users/${id}`); }
}
