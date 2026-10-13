import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { InviteUserResponse, UserResponse } from '../../core/models/auth.models';
import { UserManagementService } from '../../core/services/user-management.service';

@Component({
  selector: 'app-team',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './team.component.html',
})
export class TeamComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  readonly users = signal<UserResponse[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly invitation = signal<InviteUserResponse | null>(null);
  readonly viewingUser = signal<UserResponse | null>(null);
  readonly editingUser = signal<UserResponse | null>(null);
  readonly editName = signal('');
  readonly editEmail = signal('');
  readonly search = signal('');
  readonly page = signal(0);
  readonly pageSize = 8;
  readonly filteredUsers = computed(() => this.users().filter((u) => `${u.fullName} ${u.email}`.toLowerCase().includes(this.search().toLowerCase())).slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize));
  readonly totalPages = computed(() => Math.ceil(this.users().filter((u) => `${u.fullName} ${u.email}`.toLowerCase().includes(this.search().toLowerCase())).length / this.pageSize));

  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
  });

  constructor(
    private userManagementService: UserManagementService,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.userManagementService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Unable to load team members. Please try again.');
        this.isLoading.set(false);
      },
    });
  }

  invite(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.invitation.set(null);
    this.userManagementService.invite(this.form.getRawValue()).subscribe({
      next: (invitation) => {
        this.invitation.set(invitation);
        this.form.reset();
        this.load();
      },
      error: () => this.errorMessage.set('Unable to invite this user. Check the details and try again.'),
    });
  }

  applySearch(value: string): void { this.search.set(value); this.page.set(0); }
  goToPage(page: number): void { if (page >= 0 && page < this.totalPages()) this.page.set(page); }
  view(user: UserResponse): void { this.viewingUser.set(user); }
  startEdit(user: UserResponse): void { this.editingUser.set(user); this.editName.set(user.fullName); this.editEmail.set(user.email); }
  saveEdit(): void { const user = this.editingUser(); if (!user) return; this.userManagementService.update(user.id, { fullName: this.editName(), email: this.editEmail() }).subscribe(() => { this.editingUser.set(null); this.load(); }); }
  remove(user: UserResponse): void { this.userManagementService.delete(user.id).subscribe(() => this.load()); }
}
