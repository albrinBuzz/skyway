import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Notificacion, Pagina } from '../../core/models';
import { errorMessage } from '../../core/ui';

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
}
