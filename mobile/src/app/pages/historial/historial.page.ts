import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Notificacion, Pagina, ResumenReserva } from '../../core/models';
import { errorMessage } from '../../core/ui';

@Component({
  selector: 'app-historial',
  standalone: false,
  templateUrl: './historial.page.html',
  styleUrls: ['./historial.page.scss']
})
export class HistorialPage {
  private readonly api = inject(ApiService);

  seccion: 'activas' | 'historial' | 'cambios' = 'activas';
  reservas: Pagina<ResumenReserva> | null = null;
  cambios: Pagina<Notificacion> | null = null;
  page = 0;
  busy = false;
  error = '';
  private generation = 0;

  ionViewWillEnter() {
    void this.load(0);
  }

  cambiarSeccion(value: unknown) {
    if (value === 'activas' || value === 'historial' || value === 'cambios') {
      this.seccion = value;
      this.page = 0;
      void this.load(0);
    }
  }

  async load(page = this.page) {
    const generation = ++this.generation;
    this.busy = true;
    this.error = '';
    this.page = page;
    this.reservas = null;
    this.cambios = null;

    try {
      if (this.seccion === 'cambios') {
        const data = await firstValueFrom(this.api.notificaciones(page));
        if (generation === this.generation) this.cambios = data;
      } else {
        const data = await firstValueFrom(this.api.reservas(this.seccion, page));
        if (generation === this.generation) this.reservas = data;
      }
    } catch (e) {
      if (generation === this.generation) this.error = errorMessage(e);
    } finally {
      if (generation === this.generation) this.busy = false;
    }
  }

  icono(n: Notificacion): string {
    const value = `${n.titulo ?? ''} ${n.mensaje ?? ''}`.toLowerCase();
    if (value.includes('avión') || value.includes('avion') || value.includes('aeronave')) return 'airplane-outline';
    if (value.includes('hora') || value.includes('retras') || value.includes('demor')) return 'time-outline';
    if (value.includes('cancel')) return 'notifications-outline';
    return 'notifications-outline';
  }
}
