import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Notificacion, Pagina } from '../../core/models';
import { errorMessage } from '../../core/ui';
import { addIcons } from 'ionicons';
import {
  airplane,
  alertCircle,
  alertCircleOutline,
  arrowForwardOutline,
  briefcaseOutline,
  calendarOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  checkmarkDoneOutline,
  chevronBackOutline,
  chevronForwardOutline,
  closeCircle,
  enterOutline,
  filterOutline,
  informationCircleOutline,
  notifications,
  notificationsOutline,
  radioOutline,
  refreshOutline,
  sparkles,
  ticketOutline,
  timeOutline
} from 'ionicons/icons';

// Registrar todos los íconos usados por la pantalla de Alertas
addIcons({
  airplane,
  alertCircle,
  alertCircleOutline,
  arrowForwardOutline,
  briefcaseOutline,
  calendarOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  checkmarkDoneOutline,
  chevronBackOutline,
  chevronForwardOutline,
  closeCircle,
  enterOutline,
  filterOutline,
  informationCircleOutline,
  notifications,
  notificationsOutline,
  radioOutline,
  refreshOutline,
  sparkles,
  ticketOutline,
  timeOutline
});

export type TonoAlerta = 'ok' | 'warn' | 'bad' | 'info' | 'neutral';

export interface MetaAlerta {
  tipo: string;
  icono: string;
  tono: TonoAlerta;
}

@Component({
  selector: 'app-alertas',
  standalone: false,
  templateUrl: './alertas.page.html',
  styleUrls: ['./alertas.page.scss']
})
export class AlertasPage {
  private readonly api = inject(ApiService);

  data: Pagina<Notificacion> | null = null;
  filtro: 'todas' | 'nuevas' = 'todas';
  busy = false;
  error = '';
  page = 0;
  saving = new Set<number>();

  get visibles(): Notificacion[] {
    const items = this.data?.contenido ?? [];
    return this.filtro === 'nuevas' ? items.filter(n => !n.leido) : items;
  }

  get totalNuevas(): number {
    return this.data?.contenido?.filter(n => !n.leido).length ?? 0;
  }

  get totalTodas(): number {
    return this.data?.totalElementos ?? (this.data?.contenido?.length ?? 0);
  }

  ionViewWillEnter() {
    void this.load();
  }

  cambiarFiltro(value: unknown) {
    if (value === 'todas' || value === 'nuevas') this.filtro = value;
  }

  async load(page = this.page) {
    this.busy = true;
    this.error = '';
    this.page = page;
    this.data = null;
    try {
      this.data = await firstValueFrom(this.api.notificaciones(page));
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }

  async leer(n: Notificacion) {
    if (n.leido || this.saving.has(n.idNotificacion)) return;
    this.saving.add(n.idNotificacion);
    this.error = '';
    try {
      const updated = await firstValueFrom(this.api.leerNotificacion(n.idNotificacion));
      n.leido = updated.leido;
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.saving.delete(n.idNotificacion);
    }
  }

  async marcarTodasComoLeidas() {
    const noLeidas = (this.data?.contenido ?? []).filter(n => !n.leido && !this.saving.has(n.idNotificacion));
    if (!noLeidas.length) return;
    for (const n of noLeidas) {
      void this.leer(n);
    }
  }

  /** Clasifica semánticamente la alerta según su contenido para asignar ícono y color */
  clasificarAlerta(n: Notificacion): MetaAlerta {
    const texto = `${n.titulo ?? ''} ${n.mensaje ?? ''}`.toLowerCase();
    if (/puerta|gate|embarque/.test(texto)) {
      return { tipo: 'Puerta de embarque', icono: 'enter-outline', tono: 'warn' };
    }
    if (/demora|retras|horario|reprogram|cambio de hora/.test(texto)) {
      return { tipo: 'Cambio de horario', icono: 'time-outline', tono: 'warn' };
    }
    if (/cancelad|suspensi/.test(texto)) {
      return { tipo: 'Cancelación', icono: 'close-circle', tono: 'bad' };
    }
    if (/confirmad|check-?in|éxito|realizado/.test(texto)) {
      return { tipo: 'Confirmación', icono: 'checkmark-circle', tono: 'ok' };
    }
    if (/asiento/.test(texto)) {
      return { tipo: 'Asiento asignado', icono: 'ticket-outline', tono: 'info' };
    }
    if (/equipaje|maleta/.test(texto)) {
      return { tipo: 'Equipaje', icono: 'briefcase-outline', tono: 'info' };
    }
    return { tipo: 'Aviso operacional', icono: 'information-circle-outline', tono: 'info' };
  }

  /** Detecta si la notificación menciona una reserva para permitir navegación directa */
  reservaId(n: Notificacion): number | null {
    const match = `${n.titulo ?? ''} ${n.mensaje ?? ''}`.match(/reserva\s*#?\s*(\d+)/i);
    return match ? Number(match[1]) : null;
  }
}
