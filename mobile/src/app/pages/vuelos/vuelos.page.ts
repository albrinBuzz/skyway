import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Inicio, Pagina, ResumenReserva } from '../../core/models';
import { errorMessage } from '../../core/ui';

@Component({
  selector: 'app-vuelos',
  standalone: false,
  templateUrl: './vuelos.page.html',
  styleUrls: ['./vuelos.page.scss']
})
export class VuelosPage {
  private readonly api = inject(ApiService);

  seccion: 'proximo' | 'reservas' = 'proximo';
  inicio: Inicio | null = null;
  reservas: Pagina<ResumenReserva> | null = null;
  page = 0;
  busy = false;
  error = '';
  private generation = 0;

  ionViewWillEnter() {
    void this.load();
  }

  cambiarSeccion(value: unknown) {
    if (value === 'proximo' || value === 'reservas') this.seccion = value;
  }

  async load(page = this.page) {
    const generation = ++this.generation;
    this.busy = true;
    this.error = '';
    this.page = page;

    try {
      const [inicio, reservas] = await Promise.all([
        firstValueFrom(this.api.inicio()),
        firstValueFrom(this.api.reservas('activas', page))
      ]);
      if (generation !== this.generation) return;
      this.inicio = inicio;
      this.reservas = reservas;
    } catch (e) {
      if (generation === this.generation) this.error = errorMessage(e);
    } finally {
      if (generation === this.generation) this.busy = false;
    }
  }
}
