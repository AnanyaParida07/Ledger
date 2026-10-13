import { Component, OnInit, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { UserResponse } from '../../core/models/auth.models';
import { UserManagementService } from '../../core/services/user-management.service';

@Component({
  selector: 'app-nav-shell',
  standalone: true,
  imports: [NgIf, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './nav-shell.component.html',
  styleUrl: './nav-shell.component.css'
})
export class NavShellComponent implements OnInit {
 readonly profileOpen = signal(false);
 readonly profile = signal<UserResponse | null>(null);
 readonly profileEditing = signal(false);
 readonly profileName = signal('');
 readonly profileEmail = signal('');
 constructor(protected authService: AuthService, protected themeService: ThemeService, private users: UserManagementService) {}

 ngOnInit(): void { this.users.me().subscribe((user) => { this.profile.set(user); this.profileName.set(user.fullName); this.profileEmail.set(user.email); }); }

 toggleProfile(): void { this.profileOpen.update((open) => !open); if (!this.profile()) this.users.me().subscribe((user) => { this.profile.set(user); this.profileName.set(user.fullName); this.profileEmail.set(user.email); }); }
 saveProfile(): void { const user = this.profile(); if (!user) return; this.users.update(user.id, { fullName: this.profileName(), email: this.profileEmail() }).subscribe((updated) => { this.profile.set(updated); this.profileEditing.set(false); }); }

  logout(): void {
    this.authService.logout();
  }
}
