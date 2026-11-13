import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'system' | 'light' | 'dark';

const THEME_STORAGE_KEY = 'skyway_theme_mode';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly modeSubject = new BehaviorSubject<ThemeMode>(this.readStoredMode());
  readonly mode$ = this.modeSubject.asObservable();
  private mediaQuery: MediaQueryList | null = null;

  init(): void {
    if (typeof window === 'undefined') return;
    this.mediaQuery = window.matchMedia?.('(prefers-color-scheme: dark)') ?? null;
    this.mediaQuery?.addEventListener?.('change', () => {
      if (this.modeSubject.value === 'system') {
        this.applyTheme('system');
      }
    });
    this.applyTheme(this.modeSubject.value);
  }

  get mode(): ThemeMode {
    return this.modeSubject.value;
  }

  get isDark(): boolean {
    const m = this.modeSubject.value;
    if (m === 'dark') return true;
    if (m === 'light') return false;
    return Boolean(this.mediaQuery?.matches);
  }

  setMode(mode: ThemeMode): void {
    this.modeSubject.next(mode);
    try {
      localStorage.setItem(THEME_STORAGE_KEY, mode);
    } catch {
      // Ignorar
    }
    this.applyTheme(mode);
  }

  private readStoredMode(): ThemeMode {
    try {
      const saved = localStorage.getItem(THEME_STORAGE_KEY);
      if (saved === 'dark' || saved === 'light' || saved === 'system') {
        return saved;
      }
    } catch {
      // Ignorar
    }
    return 'light';
  }

  private applyTheme(mode: ThemeMode): void {
    if (typeof document === 'undefined') return;
    const dark =
      mode === 'dark' ||
      (mode === 'system' && Boolean(window.matchMedia?.('(prefers-color-scheme: dark)').matches));
    document.body.classList.toggle('sky-dark', dark);
    document.documentElement.classList.toggle('ion-palette-dark', dark);
    document.documentElement.style.colorScheme = dark ? 'dark' : 'light';
  }
}
