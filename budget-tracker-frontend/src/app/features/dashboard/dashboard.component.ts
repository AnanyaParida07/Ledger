import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(true);
  readonly search = signal('');
  readonly page = signal(0);
  readonly viewingCategory = signal<Category | null>(null);
  readonly editingCategory = signal<Category | null>(null);
  readonly editName = signal('');
  readonly pageSize = 8;
  readonly filteredCategories = computed(() => this.categories().filter((c) => c.name.toLowerCase().includes(this.search().toLowerCase())).slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));
  readonly totalPages = computed(() => Math.ceil(this.categories().filter((c) => c.name.toLowerCase().includes(this.search().toLowerCase())).length / this.pageSize));

  readonly form;

  constructor(
    private categoryService: CategoryService,
    private fb: FormBuilder,
    protected authService: AuthService,
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.categoryService.list().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  addCategory(): void {
    if (this.form.invalid) {
      return;
    }
    this.categoryService.create(this.form.getRawValue().name!).subscribe(() => {
      this.form.reset();
      this.load();
    });
  }

  applySearch(value: string): void { this.search.set(value); this.page.set(0); }
  goToPage(page: number): void { if (page >= 0 && page < this.totalPages()) this.page.set(page); }
  view(category: Category): void { this.viewingCategory.set(category); }
  startEdit(category: Category): void { this.editingCategory.set(category); this.editName.set(category.name); }
  saveEdit(): void {
    const category = this.editingCategory();
    if (!category || !this.editName().trim()) return;
    this.categoryService.update(category.id, this.editName().trim()).subscribe(() => {
      this.editingCategory.set(null);
      this.load();
    });
  }
  remove(category: Category): void {
    this.categoryService.delete(category.id).subscribe(() => this.load());
  }
}
