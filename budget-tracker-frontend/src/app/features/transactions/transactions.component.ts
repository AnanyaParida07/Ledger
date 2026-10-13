import { Component, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { TransactionService } from "../../core/services/transaction.service";
import { CategoryService } from "../../core/services/category.service";
import { Category } from "../../core/models/auth.models";
import {
  TransactionRequest,
  TransactionResponse,
} from "../../core/models/transaction.models";

function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

@Component({
  selector: "app-transactions",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: "./transactions.component.html",
})

export class TransactionsComponent implements OnInit {
  readonly transactions = signal<TransactionResponse[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(true);
  readonly month = signal(currentMonth());
  readonly categoryFilter = signal("");
  readonly typeFilter = signal("");
  readonly minAmount = signal("");
  readonly maxAmount = signal("");
  readonly startDate = signal("");
  readonly endDate = signal("");
  readonly sortBy = signal("transactionDate");
  readonly direction = signal("desc");
  readonly page = signal(0);
  readonly pageSize = signal(10);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly editingId = signal<string | null>(null);
  readonly viewingTransaction = signal<TransactionResponse | null>(null);

  readonly form;

  constructor(
    private transactionService: TransactionService,
    private categoryService: CategoryService,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      categoryId: ["", Validators.required],
      amount: [
        null as number | null,
        [Validators.required, Validators.min(0.01)],
      ],
      description: [""],
      type: ["EXPENSE" as "EXPENSE" | "INCOME", Validators.required],
      transactionDate: [
        new Date().toISOString().slice(0, 10),
        Validators.required,
      ],
    });
  }

  ngOnInit(): void {
    this.categoryService
      .list()
      .subscribe((categories) => this.categories.set(categories));
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.transactionService
      .search({
        startDate: this.startDate() || `${this.month()}-01`,
        endDate:
          this.endDate() ||
          new Date(
            Number(this.month().slice(0, 4)),
            Number(this.month().slice(5)),
            0,
          )
            .toISOString()
            .slice(0, 10),
        categoryId: this.categoryFilter(),
        type: this.typeFilter(),
        minAmount: this.minAmount(),
        maxAmount: this.maxAmount(),
        page: this.page(),
        size: this.pageSize(),
        sortBy: this.sortBy(),
        direction: this.direction(),
      })
      .subscribe({
        next: (result) => {
          this.transactions.set(result.content);
          this.totalPages.set(result.totalPages);
          this.totalElements.set(result.totalElements);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }

  applyFilters(): void {
    this.page.set(0);
    this.load();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.page.set(page);
      this.load();
    }
  }

  changeSort(sortBy: string): void {
    this.sortBy.set(sortBy);
    this.direction.set(this.direction() === "asc" ? "desc" : "asc");
    this.page.set(0);
    this.load();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.getRawValue() as TransactionRequest;
    const operation = this.editingId()
      ? this.transactionService.update(this.editingId()!, request)
      : this.transactionService.create(request);
    operation.subscribe(() => {
      this.form.patchValue({ amount: null, description: "" });
      this.editingId.set(null);
      this.load();
    });
  }

  edit(transaction: TransactionResponse): void {
    this.editingId.set(transaction.id);
    this.form.patchValue({
      categoryId: transaction.categoryId,
      amount: transaction.amount,
      description: transaction.description,
      type: transaction.type,
      transactionDate: transaction.transactionDate,
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({
      type: "EXPENSE",
      transactionDate: new Date().toISOString().slice(0, 10),
    });
  }
  
  view(transaction: TransactionResponse): void {
    this.viewingTransaction.set(transaction);
  }

  remove(id: string): void {
    this.transactionService.delete(id).subscribe(() => this.load());
  }
}
