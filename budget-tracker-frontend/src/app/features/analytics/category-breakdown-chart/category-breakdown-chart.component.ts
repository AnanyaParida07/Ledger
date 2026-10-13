import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Color, LegendPosition, PieChartModule, ScaleType } from '@swimlane/ngx-charts';

import { CategorySpendResponse } from '../../../core/models/analytics.models';

@Component({
  selector: 'app-category-breakdown-chart',
  standalone: true,
  imports: [PieChartModule],
  templateUrl: './category-breakdown-chart.component.html',
  styleUrl: './category-breakdown-chart.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryBreakdownChartComponent {
  readonly legendPosition = LegendPosition.Below;
  readonly data = input.required<CategorySpendResponse[]>();
  readonly results = computed(() => this.data().map((item) => ({ name: item.categoryName, value: item.totalAmount })));
  readonly colorScheme: Color = {
    name: 'ledger-category-spend',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#1f6f52', '#d78645', '#6089bf', '#af658f', '#7f9f62', '#aa7dbd', '#d05f54', '#5b9b9a'],
  };
}
