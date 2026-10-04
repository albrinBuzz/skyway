import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Pagina, Notificacion } from '../../core/models';
import { errorMessage } from '../../core/ui';
@Component({ selector: 'app-notificaciones', standalone: false, templateUrl: './notificaciones.page.html' })
export class NotificacionesPage {
  private api = inject(ApiService); data: Pagina<Notificacion> | null = null;
  busy = false; error = ''; page = 0; saving = new Set<number>();
  ionViewWillEnter() { void this.load(); }
  async load(page = this.page) {
    this.busy = true; this.error = ''; this.page = page; this.data = null;
    try { this.data = await firstValueFrom(this.api.notificaciones(page)); }
    catch(e) { this.error = errorMessage(e); } finally { this.busy = false; }
  }
  async leer(n: Notificacion) {
    if(n.leido || this.saving.has(n.idNotificacion)) return;
    this.saving.add(n.idNotificacion); this.error = '';
    try { const updated = await firstValueFrom(this.api.leerNotificacion(n.idNotificacion)); n.leido = updated.leido; }
    catch(e) { this.error = errorMessage(e); } finally { this.saving.delete(n.idNotificacion); }
  }
}
