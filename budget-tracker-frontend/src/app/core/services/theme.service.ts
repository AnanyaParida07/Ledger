import { Injectable, signal } from '@angular/core';

const THEME_KEY = 'ledger_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly isDark = signal(localStorage.getItem(THEME_KEY) === 'dark');

  constructor() {
    this.apply(this.isDark());
  }

  toggle(): void {
    const dark = !this.isDark();
    this.isDark.set(dark);
    localStorage.setItem(THEME_KEY, dark ? 'dark' : 'light');
    this.apply(dark);
  }

  private apply(dark: boolean): void {
    document.documentElement.dataset['theme'] = dark ? 'dark' : 'light';
  }
}
