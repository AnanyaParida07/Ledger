import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { RecurringTransactionRequest, RecurringTransactionResponse, RecurringTransactionUpdateRequest } from '../models/transaction.models';

@Injectable({ providedIn: 'root' })
export class RecurringTransactionService {
  constructor(private http: HttpClient) {}

  list(): Observable<RecurringTransactionResponse[]> {
    return this.http.get<RecurringTransactionResponse[]>(`${environment.apiUrl}/recurring-transactions`);
  }

  create(request: RecurringTransactionRequest): Observable<RecurringTransactionResponse> {
    return this.http.post<RecurringTransactionResponse>(`${environment.apiUrl}/recurring-transactions`, request);
  }

  update(id: string, request: RecurringTransactionUpdateRequest): Observable<RecurringTransactionResponse> {
    return this.http.put<RecurringTransactionResponse>(`${environment.apiUrl}/recurring-transactions/${id}`, request);
  }

  setActive(id: string, active: boolean): Observable<void> {
    return this.http.patch<void>(`${environment.apiUrl}/recurring-transactions/${id}/active`, null, {
      params: { active },
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/recurring-transactions/${id}`);
  }
}
