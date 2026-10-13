import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { RecurringTransactionService } from '../../core/services/recurring-transaction.service';
import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/auth.models';
import { RecurringTransactionRequest, RecurringTransactionResponse, RecurringTransactionUpdateRequest } from '../../core/models/transaction.models';

@Component({
  selector: 'app-recurring',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './recurring.component.html',
})
export class RecurringComponent implements OnInit {
  readonly recurring = signal<RecurringTransactionResponse[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(true);
  readonly search = signal('');
  readonly page = signal(0);
  readonly viewingRecurring = signal<RecurringTransactionResponse | null>(null);
  readonly editingId = signal<string | null>(null);
  readonly pageSize = 8;
  readonly visibleRecurring = computed(() => this.recurring().filter((r) => `${r.categoryName} ${r.description || ''}`.toLowerCase().includes(this.search().toLowerCase())).slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));
  readonly totalPages = computed(() => Math.ceil(this.recurring().filter((r) => `${r.categoryName} ${r.description || ''}`.toLowerCase().includes(this.search().toLowerCase())).length / this.pageSize));

  readonly form;

  constructor(
    private recurringService: RecurringTransactionService,
    private categoryService: CategoryService,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      categoryId: ['', Validators.required],
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
      description: [''],
      type: ['EXPENSE' as 'EXPENSE' | 'INCOME', Validators.required],
      frequency: ['MONTHLY' as 'DAILY' | 'WEEKLY' | 'MONTHLY', Validators.required],
      startDate: [new Date().toISOString().slice(0, 10), Validators.required],
    });
  }

  ngOnInit(): void {
    this.categoryService.list().subscribe((categories) => this.categories.set(categories));
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.recurringService.list().subscribe({
      next: (recurring) => {
        this.recurring.set(recurring);
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
    const raw = this.form.getRawValue();
    const operation = this.editingId()
      ? this.recurringService.update(this.editingId()!, {
          categoryId: raw.categoryId!,
          amount: raw.amount!,
          description: raw.description || '',
          type: raw.type!,
          frequency: raw.frequency!,
          nextRunDate: raw.startDate!,
          active: this.recurring().find((item) => item.id === this.editingId())?.active ?? true,
        } as RecurringTransactionUpdateRequest)
      : this.recurringService.create(raw as RecurringTransactionRequest);
    operation.subscribe(() => {
      this.form.patchValue({ amount: null, description: '' });
      this.editingId.set(null);
      this.load();
    });
  }

  toggleActive(item: RecurringTransactionResponse): void {
    this.recurringService.setActive(item.id, !item.active).subscribe(() => this.load());
  }

  applySearch(value: string): void { this.search.set(value); this.page.set(0); }
  goToPage(page: number): void { if (page >= 0 && page < this.totalPages()) this.page.set(page); }
  view(item: RecurringTransactionResponse): void { this.viewingRecurring.set(item); }
  edit(item: RecurringTransactionResponse): void {
    this.editingId.set(item.id);
    this.form.patchValue({
      categoryId: item.categoryId,
      amount: item.amount,
      description: item.description,
      type: item.type,
      frequency: item.frequency,
      startDate: item.nextRunDate,
    });
  }
  cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({
      categoryId: '',
      amount: null,
      description: '',
      type: 'EXPENSE',
      frequency: 'MONTHLY',
      startDate: new Date().toISOString().slice(0, 10),
    });
  }
  remove(item: RecurringTransactionResponse): void {
    this.recurringService.delete(item.id).subscribe(() => this.load());
  }
}
