import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { DetalleReserva, PasajeroTicket, Vuelo, Tarifa } from '../../core/models';
import { errorMessage, featureValue, flights } from '../../core/ui';

@Component({ selector: 'app-reserva-detalle', standalone: false, templateUrl: './reserva-detalle.page.html', styleUrls: ['./reserva-detalle.page.scss'] })
export class ReservaDetallePage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly alerts = inject(AlertController);

  readonly id = Number(this.route.snapshot.paramMap.get('id'));
  data: DetalleReserva | null = null;
  tickets: Record<number, PasajeroTicket[]> = {};
  ticketErrors: Record<number, string> = {};
  busy = false;
  checking = false;
  error = '';
  featureValue = featureValue;
  private generation = 0;

  get vuelos(): Vuelo[] { return this.data ? flights(this.data) : []; }
  hasBaggage(tarifa: Tarifa) { return tarifa.caracteristicas.some(c => /equipaje|maleta|franquicia/i.test(c.nombre)); }
  personas(idVuelo: number): PasajeroTicket[] { return this.tickets[idVuelo] ?? []; }

  ionViewWillEnter() { void this.load(); }

  async load() {
    const generation = ++this.generation;
    this.busy = true;
    this.error = '';
    this.data = null;
    this.tickets = {};
    this.ticketErrors = {};
    try {
      const reserva = await firstValueFrom(this.api.reserva(this.id));
      if (generation !== this.generation) return;
      this.data = reserva;
      const idsVuelos = [...new Set(flights(reserva).map(v => v.idVuelo))];
      // Una reserva puede incluir conexiones; lista de pasajeros separada por vuelo.
      await Promise.all(idsVuelos.map(async idVuelo => {
        try {
          const personas = await firstValueFrom(this.api.ticketsReserva(this.id, idVuelo));
          if (generation === this.generation) this.tickets[idVuelo] = personas;
        } catch (e) {
          if (generation === this.generation) this.ticketErrors[idVuelo] = errorMessage(e);
        }
      }));
    } catch (e) {
      if (generation === this.generation) this.error = errorMessage(e);
    } finally {
      if (generation === this.generation) this.busy = false;
    }
  }

  async checkin() {
    if (this.checking || !this.data) return;
    const alert = await this.alerts.create({
      header: 'Confirmar check-in',
      message: 'El check-in se registrará para esta reserva y solo podrá completarse cuando cada pasajero tenga su asiento asignado.',
      buttons: [{ text: 'Volver', role: 'cancel' }, { text: 'Confirmar', role: 'confirm' }]
    });
    await alert.present();
    if ((await alert.onDidDismiss()).role !== 'confirm') return;
    this.checking = true;
    this.error = '';
    try {
      await firstValueFrom(this.api.checkin(this.id));
      await this.load();
      const flight = this.vuelos[0];
      const pasajero = flight && this.personas(flight.idVuelo).find(p => p.asiento);
      if (flight && pasajero) {
        await this.router.navigate(['/vuelos/reservas', this.id, 'vuelos', flight.idVuelo, 'tickets', pasajero.rut]);
      }
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.checking = false;
    }
  }
}
