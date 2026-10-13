import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PagedResponse, TransactionRequest, TransactionResponse } from '../models/transaction.models';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  constructor(private http: HttpClient) {}

  list(month: string): Observable<TransactionResponse[]> {
    return this.http.get<TransactionResponse[]>(`${environment.apiUrl}/transactions`, { params: { month } });
  }

  search(params: Record<string, string | number | undefined>): Observable<PagedResponse<TransactionResponse>> {
    let httpParams: Record<string, string> = {};
    for (const [key, value] of Object.entries(params)) if (value !== undefined && value !== '') httpParams[key] = String(value);
    return this.http.get<PagedResponse<TransactionResponse>>(`${environment.apiUrl}/transactions/search`, { params: httpParams });
  }

  create(request: TransactionRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${environment.apiUrl}/transactions`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/transactions/${id}`);
  }

  update(id: string, request: TransactionRequest & { editReason?: string }): Observable<TransactionResponse> {
    return this.http.put<TransactionResponse>(`${environment.apiUrl}/transactions/${id}`, request);
  }
}
