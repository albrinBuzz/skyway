import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ToastController } from '@ionic/angular';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';
import { AuthStore } from './auth.store';
import { HapticsService } from './haptics.service';

const PUSH_OPTIN_KEY = 'skyway_push_alerts_enabled';
const SEEN_ALERT_IDS_KEY = 'skyway_seen_live_alert_ids';

@Injectable({ providedIn: 'root' })
export class NotificationsLiveService {
  private readonly api = inject(ApiService);
  private readonly store = inject(AuthStore);
  private readonly toasts = inject(ToastController);
  private readonly router = inject(Router);
  private readonly haptics = inject(HapticsService);

  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  private pollTimer: ReturnType<typeof setInterval> | null = null;
  private initialized = false;
  private checkinRemindedReservaId: number | null = null;

  get pushEnabled(): boolean {
    try {
      const val = localStorage.getItem(PUSH_OPTIN_KEY);
      return val === null ? true : val === '1';
    } catch {
      return true;
    }
  }

  setPushEnabled(enabled: boolean): void {
    try {
      localStorage.setItem(PUSH_OPTIN_KEY, enabled ? '1' : '0');
    } catch {
      // Ignorar
    }
    if (enabled && typeof Notification !== 'undefined' && Notification.permission === 'default') {
      void Notification.requestPermission().catch(() => undefined);
    }
  }

  start(): void {
    if (this.initialized) return;
    this.initialized = true;
    void this.syncNow(true);
    this.pollTimer = setInterval(() => {
      if (this.store.authenticated && (typeof document === 'undefined' || document.visibilityState === 'visible')) {
        void this.syncNow(false);
      }
    }, 45000);
  }

  setUnreadCount(count: number): void {
    this.unreadCountSubject.next(Math.max(0, count || 0));
  }

  decrementUnread(): void {
    this.setUnreadCount(this.unreadCountSubject.value - 1);
  }

  async syncNow(initial = false): Promise<void> {
    if (!this.store.authenticated) {
      this.unreadCountSubject.next(0);
      return;
    }
    try {
      const inicio = await firstValueFrom(this.api.inicio());
      const prev = this.unreadCountSubject.value;
      const next = inicio.notificacionesSinLeer || 0;
      this.unreadCountSubject.next(next);

      if (this.pushEnabled && !initial && next > prev) {
        const page = await firstValueFrom(this.api.notificaciones(0, 5));
        const newest = page.contenido.find(n => !n.leido && !this.alreadyNotified(n.idNotificacion));
        if (newest) {
          this.markNotified(newest.idNotificacion);
          await this.emitLiveAlert(
            newest.titulo || 'Nueva actualización de vuelo',
            newest.mensaje || 'Tienes una nueva alerta operacional en SkyWay.',
            '/alertas',
            'Ver alerta'
          );
        }
      }

      // Recordatorio automático cuando el próximo vuelo entra en ventana de check-in (<= 48h)
      if (this.pushEnabled && inicio.proximoVuelo?.vuelo?.fechaHoraSalida) {
        const idRes = inicio.proximoVuelo.idReserva;
        if (this.checkinRemindedReservaId !== idRes) {
          const dep = new Date(inicio.proximoVuelo.vuelo.fechaHoraSalida).getTime();
          const diffH = (dep - Date.now()) / 3600000;
          if (diffH > 1 && diffH <= 48) {
            this.checkinRemindedReservaId = idRes;
            if (!initial) {
              await this.emitLiveAlert(
                'Check-in disponible',
                `Tu vuelo ${inicio.proximoVuelo.vuelo.numeroVuelo || ''} sale pronto. Completa tu check-in para obtener el QR.`,
                `/vuelos/reservas/${idRes}`,
                'Ir al vuelo'
              );
            }
          }
        }
      }
    } catch {
      // Silencioso en polling en segundo plano
    }
  }

  private async emitLiveAlert(title: string, message: string, route: string, actionLabel: string): Promise<void> {
    this.haptics.medium();

    if (typeof Notification !== 'undefined' && Notification.permission === 'granted' && typeof document !== 'undefined' && document.hidden) {
      try {
        new Notification(title, { body: message });
      } catch {
        // Ignorar si Notification constructor no está soportado en WebView
      }
    }

    const toast = await this.toasts.create({
      header: title,
      message,
      duration: 5000,
      position: 'top',
      color: 'primary',
      buttons: [
        {
          text: actionLabel,
          handler: () => {
            void this.router.navigateByUrl(route);
          }
        },
        { text: 'Cerrar', role: 'cancel' }
      ]
    });
    await toast.present();
  }

  private alreadyNotified(id: number): boolean {
    try {
      const raw = localStorage.getItem(SEEN_ALERT_IDS_KEY);
      if (!raw) return false;
      const arr = JSON.parse(raw) as number[];
      return Array.isArray(arr) && arr.includes(id);
    } catch {
      return false;
    }
  }

  private markNotified(id: number): void {
    try {
      const raw = localStorage.getItem(SEEN_ALERT_IDS_KEY);
      const arr = raw ? (JSON.parse(raw) as number[]) : [];
      const next = [id, ...(Array.isArray(arr) ? arr.filter(x => x !== id) : [])].slice(0, 30);
      localStorage.setItem(SEEN_ALERT_IDS_KEY, JSON.stringify(next));
    } catch {
      // Ignorar
    }
  }
}
