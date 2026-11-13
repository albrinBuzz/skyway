import { Injectable } from '@angular/core';

const HAPTICS_ENABLED_KEY = 'skyway_haptics_enabled';

@Injectable({ providedIn: 'root' })
export class HapticsService {
  get enabled(): boolean {
    try {
      const raw = localStorage.getItem(HAPTICS_ENABLED_KEY);
      return raw === null ? true : raw === '1';
    } catch {
      return true;
    }
  }

  setEnabled(value: boolean): void {
    try {
      localStorage.setItem(HAPTICS_ENABLED_KEY, value ? '1' : '0');
    } catch {
      // Ignorar
    }
  }

  light(): void {
    this.pulse(12);
  }

  medium(): void {
    this.pulse(24);
  }

  success(): void {
    this.pulse([16, 40, 24]);
  }

  private pulse(pattern: number | number[]): void {
    if (!this.enabled || typeof navigator === 'undefined') return;
    try {
      const cap = (window as unknown as { Capacitor?: { Plugins?: { Haptics?: { impact?: (o: { style: string }) => Promise<void> } } } })?.Capacitor;
      if (cap?.Plugins?.Haptics?.impact) {
        void cap.Plugins.Haptics.impact({ style: Array.isArray(pattern) ? 'MEDIUM' : 'LIGHT' });
        return;
      }
      navigator.vibrate?.(pattern);
    } catch {
      // Ignorar en dispositivos/navegadores sin vibración
    }
  }
}
