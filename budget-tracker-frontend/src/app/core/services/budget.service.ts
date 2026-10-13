import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { BudgetRequest, BudgetStatusResponse, BudgetUpdateRequest } from '../models/transaction.models';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  constructor(private http: HttpClient) {}

  status(month: string): Observable<BudgetStatusResponse[]> {
    return this.http.get<BudgetStatusResponse[]>(`${environment.apiUrl}/budgets`, { params: { month } });
  }

  setBudget(request: BudgetRequest): Observable<BudgetStatusResponse> {
    return this.http.post<BudgetStatusResponse>(`${environment.apiUrl}/budgets`, request);
  }

  update(categoryId: string, month: string, request: BudgetUpdateRequest): Observable<BudgetStatusResponse> {
    return this.http.put<BudgetStatusResponse>(`${environment.apiUrl}/budgets/${categoryId}`, request, { params: { month } });
  }

  delete(categoryId: string, month: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/budgets/${categoryId}`, { params: { month } });
  }
}
