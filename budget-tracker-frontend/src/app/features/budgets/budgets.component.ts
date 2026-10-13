import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { BudgetService } from '../../core/services/budget.service';
import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/auth.models';
import { BudgetRequest, BudgetStatusResponse } from '../../core/models/transaction.models';
import { AuthService } from '../../core/services/auth.service';

function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './budgets.component.html',
})
export class BudgetsComponent implements OnInit {
  readonly budgets = signal<BudgetStatusResponse[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(true);
  readonly month = signal(currentMonth());
  readonly search = signal('');
  readonly page = signal(0);
  readonly viewingBudget = signal<BudgetStatusResponse | null>(null);
  readonly editingBudget = signal<BudgetStatusResponse | null>(null);
  readonly editLimit = signal<number | null>(null);
  readonly pageSize = 8;
  readonly visibleBudgets = computed(() => this.budgets().filter((b) => b.categoryName.toLowerCase().includes(this.search().toLowerCase())).slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));
  readonly totalPages = computed(() => Math.ceil(this.budgets().filter((b) => b.categoryName.toLowerCase().includes(this.search().toLowerCase())).length / this.pageSize));

  readonly form;

  constructor(
    private budgetService: BudgetService,
    private categoryService: CategoryService,
    private fb: FormBuilder,
    protected authService: AuthService,
  ) {
    this.form = this.fb.group({
      categoryId: ['', Validators.required],
      monthlyLimit: [null as number | null, [Validators.required, Validators.min(0.01)]],
    });
  }

  ngOnInit(): void {
    this.categoryService.list().subscribe((categories) => this.categories.set(categories));
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.budgetService.status(this.month()).subscribe({
      next: (budgets) => {
        this.budgets.set(budgets);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request: BudgetRequest = {
      categoryId: this.form.getRawValue().categoryId!,
      monthlyLimit: this.form.getRawValue().monthlyLimit!,
      month: this.month(),
    };
    this.budgetService.setBudget(request).subscribe(() => {
      this.form.patchValue({ monthlyLimit: null });
      this.load();
    });
  }

  applySearch(value: string): void { this.search.set(value); this.page.set(0); }
  goToPage(page: number): void { if (page >= 0 && page < this.totalPages()) this.page.set(page); }
  view(budget: BudgetStatusResponse): void { this.viewingBudget.set(budget); }
  startEdit(budget: BudgetStatusResponse): void { this.editingBudget.set(budget); this.editLimit.set(budget.monthlyLimit); }
  saveEdit(): void {
    const budget = this.editingBudget();
    const monthlyLimit = this.editLimit();
    if (!budget || monthlyLimit === null || monthlyLimit <= 0) return;
    this.budgetService.update(budget.categoryId, budget.month, { monthlyLimit }).subscribe(() => {
      this.editingBudget.set(null);
      this.load();
    });
  }
  remove(budget: BudgetStatusResponse): void {
    this.budgetService.delete(budget.categoryId, budget.month).subscribe(() => this.load());
  }
}
