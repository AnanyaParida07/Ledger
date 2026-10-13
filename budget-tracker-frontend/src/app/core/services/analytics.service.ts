import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CategorySpendResponse, MonthlyTrendResponse, SummaryResponse } from '../models/analytics.models';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  constructor(private http: HttpClient) {}

  summary(month: string): Observable<SummaryResponse> {
    return this.http.get<SummaryResponse>(`${environment.apiUrl}/analytics/summary`, { params: { month } });
  }

  spendByCategory(month: string): Observable<CategorySpendResponse[]> {
    return this.http.get<CategorySpendResponse[]>(`${environment.apiUrl}/analytics/spend-by-category`, {
      params: { month },
    });
  }

  trend(months: number): Observable<MonthlyTrendResponse[]> {
    return this.http.get<MonthlyTrendResponse[]>(`${environment.apiUrl}/analytics/trend`, { params: { months } });
  }
}
