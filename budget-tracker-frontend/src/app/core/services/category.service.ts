import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Category } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  constructor(private http: HttpClient) {}

  list(): Observable<Category[]> {
    return this.http.get<Category[]>(`${environment.apiUrl}/categories`);
  }

  create(name: string): Observable<Category> {
    return this.http.post<Category>(`${environment.apiUrl}/categories`, { name });
  }

  update(id: string, name: string): Observable<Category> {
    return this.http.put<Category>(`${environment.apiUrl}/categories/${id}`, { name });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/categories/${id}`);
  }
}
