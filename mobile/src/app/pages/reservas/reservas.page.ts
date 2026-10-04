import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Pagina, ResumenReserva } from '../../core/models';
import { errorMessage } from '../../core/ui';
@Component({ selector: 'app-reservas', standalone: false, templateUrl: './reservas.page.html' })
export class ReservasPage {
  private api = inject(ApiService); grupo: 'activas' | 'historial' = 'activas';
  data: Pagina<ResumenReserva> | null = null; page = 0; busy = false; error = ''; private generation = 0;
  ionViewWillEnter() { void this.load(); }
  change(value: unknown) { if(value === 'activas' || value === 'historial') { this.grupo = value; this.page = 0; void this.load(); } }
  async load(page = this.page) {
    const generation = ++this.generation; this.busy = true; this.error = ''; this.page = page; this.data = null;
    try { const result = await firstValueFrom(this.api.reservas(this.grupo, page)); if(generation === this.generation) this.data = result; }
    catch(e) { if(generation === this.generation) this.error = errorMessage(e); }
    finally { if(generation === this.generation) this.busy = false; }
  }
}
