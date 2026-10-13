import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Color, LineChartModule, ScaleType } from '@swimlane/ngx-charts';

import { MonthlyTrendResponse } from '../../../core/models/analytics.models';

@Component({
  selector: 'app-monthly-trend-chart',
  standalone: true,
  imports: [LineChartModule],
  templateUrl: './monthly-trend-chart.component.html',
  styleUrl: './monthly-trend-chart.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonthlyTrendChartComponent {
  readonly data = input.required<MonthlyTrendResponse[]>();
  readonly results = computed(() => [
    { name: 'Income', series: this.data().map((item) => ({ name: item.month, value: item.income })) },
    { name: 'Expense', series: this.data().map((item) => ({ name: item.month, value: item.expense })) },
  ]);
  readonly colorScheme: Color = {
    name: 'ledger-income-expense',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#1f6f52', '#b3402a'],
  };
}
