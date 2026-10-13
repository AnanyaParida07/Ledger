import { Component, ElementRef, HostListener, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CategorySpendResponse, MonthlyTrendResponse, SummaryResponse } from '../../core/models/analytics.models';
import { AnalyticsService } from '../../core/services/analytics.service';
import { CategoryBreakdownChartComponent } from './category-breakdown-chart/category-breakdown-chart.component';
import { MonthlyTrendChartComponent } from './monthly-trend-chart/monthly-trend-chart.component';

function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, CategoryBreakdownChartComponent, MonthlyTrendChartComponent],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.css',
})
export class AnalyticsComponent implements OnInit {
  readonly month = signal(currentMonth());
  readonly monthPickerOpen = signal(false);
  readonly pickerYear = signal(Number(this.month().slice(0, 4)));
  readonly monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ];
  readonly selectedMonthLabel = computed(() => {
    const [year, month] = this.month().split('-').map(Number);
    return new Date(year, month - 1, 1).toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  });
  readonly trendMonths = signal(6);
  readonly summary = signal<SummaryResponse | null>(null);
  readonly categoryBreakdown = signal<CategorySpendResponse[]>([]);
  readonly trend = signal<MonthlyTrendResponse[]>([]);
  readonly hasTrendData = computed(() =>
    this.trend().some((point) => point.income !== 0 || point.expense !== 0 || point.net !== 0),
  );
  readonly isSummaryLoading = signal(true);
  readonly isBreakdownLoading = signal(true);
  readonly isTrendLoading = signal(true);
  readonly summaryError = signal(false);
  readonly breakdownError = signal(false);
  readonly trendError = signal(false);

  constructor(private analyticsService: AnalyticsService, private elementRef: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    this.loadMonthAnalytics();
    this.loadTrend();
  }

  onMonthChange(month: string): void {
    if (!month) return;
    this.month.set(month);
    this.loadMonthAnalytics();
  }

  toggleMonthPicker(): void {
    if (!this.monthPickerOpen()) this.pickerYear.set(Number(this.month().slice(0, 4)));
    this.monthPickerOpen.update((open) => !open);
  }

  @HostListener('document:keydown.escape')
  closeMonthPicker(): void {
    this.monthPickerOpen.set(false);
  }

  changePickerYear(offset: number): void {
    this.pickerYear.update((year) => year + offset);
  }

  selectMonth(monthIndex: number): void {
    const month = `${this.pickerYear()}-${String(monthIndex + 1).padStart(2, '0')}`;
    this.onMonthChange(month);
    this.closeMonthPicker();
  }

  isSelectedMonth(monthIndex: number): boolean {
    const [year, month] = this.month().split('-').map(Number);
    return year === this.pickerYear() && month === monthIndex + 1;
  }

  selectCurrentMonth(): void {
    const now = currentMonth();
    this.pickerYear.set(Number(now.slice(0, 4)));
    this.onMonthChange(now);
    this.closeMonthPicker();
  }

  @HostListener('document:click', ['$event'])
  closePickerOnOutsideClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) this.closeMonthPicker();
  }

  onTrendMonthsChange(months: string): void {
    this.trendMonths.set(Number(months));
    this.loadTrend();
  }

  private loadMonthAnalytics(): void {
    this.isSummaryLoading.set(true);
    this.summaryError.set(false);
    this.analyticsService.summary(this.month()).subscribe({
      next: (summary) => { this.summary.set(summary); this.isSummaryLoading.set(false); },
      error: () => { this.summaryError.set(true); this.isSummaryLoading.set(false); },
    });

    this.isBreakdownLoading.set(true);
    this.breakdownError.set(false);
    this.analyticsService.spendByCategory(this.month()).subscribe({
      next: (breakdown) => { this.categoryBreakdown.set(breakdown); this.isBreakdownLoading.set(false); },
      error: () => { this.breakdownError.set(true); this.isBreakdownLoading.set(false); },
    });
  }

  private loadTrend(): void {
    this.isTrendLoading.set(true);
    this.trendError.set(false);
    this.analyticsService.trend(this.trendMonths()).subscribe({
      next: (trend) => { this.trend.set(trend); this.isTrendLoading.set(false); },
      error: () => { this.trendError.set(true); this.isTrendLoading.set(false); },
    });
  }
}
