import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { DetalleReserva, Vuelo, Tarifa } from '../../core/models';
import { errorMessage, featureValue, flights } from '../../core/ui';
@Component({ selector: 'app-reserva-detalle', standalone: false, templateUrl: './reserva-detalle.page.html' })
export class ReservaDetallePage {
  private api = inject(ApiService); private route = inject(ActivatedRoute); private router = inject(Router); private alerts = inject(AlertController);
  id = Number(this.route.snapshot.paramMap.get('id')); data: DetalleReserva | null = null;
  busy = false; checking = false; error = ''; featureValue = featureValue;
  get vuelos(): Vuelo[] { return this.data ? flights(this.data) : []; }
  hasBaggage(tarifa: Tarifa) { return tarifa.caracteristicas.some(c => /equipaje|maleta|franquicia/i.test(c.nombre)); }
  ionViewWillEnter() { void this.load(); }
  async load() {
    this.busy = true; this.error = ''; this.data = null;
    try { this.data = await firstValueFrom(this.api.reserva(this.id)); }
    catch(e) { this.error = errorMessage(e); } finally { this.busy = false; }
  }
  async checkin() {
    if(this.checking || !this.data) return;
    const alert = await this.alerts.create({ header: 'Confirmar check-in', message: 'Se registrará el check-in para toda esta reserva. Después, cada pasajero podrá consultar su pase de simulación.', buttons: [{ text: 'Volver', role: 'cancel' }, { text: 'Confirmar', role: 'confirm' }] });
    await alert.present(); if((await alert.onDidDismiss()).role !== 'confirm') return;
    this.checking = true; this.error = '';
    try {
      await firstValueFrom(this.api.checkin(this.id)); await this.load();
      const flight = this.vuelos.find(v => !!v.asiento);
      if(flight) await this.router.navigate(['/reservas',this.id,'vuelos',flight.idVuelo,'pase']);
    } catch(e) { this.error = errorMessage(e); } finally { this.checking = false; }
  }
}
