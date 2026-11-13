import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { addIcons } from 'ionicons';
import {
  airplane, alertCircle, briefcaseOutline, businessOutline, calendarOutline, cashOutline, checkmarkCircle,
  chevronDownOutline, closeCircle, downloadOutline, enterOutline, helpCircle, informationCircle, locationOutline, peopleOutline, personCircleOutline,
  pricetagOutline, refreshOutline, shieldCheckmarkOutline, ticketOutline, timeOutline
} from 'ionicons/icons';
import { Caracteristica, DetalleReserva, Itinerario, PasajeroTicket, Vuelo, Tarifa } from '../../core/models';
import { errorMessage, featureValue, flights } from '../../core/ui';

// Íconos usados solo por esta pantalla (el resto se registra en AppModule).
addIcons({
  airplane, alertCircle, briefcaseOutline, businessOutline, calendarOutline, cashOutline, checkmarkCircle,
  chevronDownOutline, closeCircle, downloadOutline, enterOutline, helpCircle, informationCircle, locationOutline, peopleOutline, personCircleOutline,
  pricetagOutline, refreshOutline, shieldCheckmarkOutline, ticketOutline, timeOutline
});

/** Tono visual de un chip; siempre se acompaña de ícono + texto (nunca solo color). */
export type Tono = 'ok' | 'warn' | 'bad' | 'info' | 'neutral';
export interface Etiqueta { tono: Tono; icono: string; texto: string; }

const CLP = new Intl.NumberFormat('es-CL', { maximumFractionDigits: 0 });

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
  descargandoPdf = false;
  error = '';
  featureValue = featureValue;
  openSections = new Set<string>();
  private generation = 0;

  isOpen(key: string): boolean { return this.openSections.has(key); }

  toggleSection(key: string): void {
    if (this.openSections.has(key)) {
      this.openSections.delete(key);
    } else {
      this.openSections.add(key);
    }
  }

  get vuelos(): Vuelo[] { return this.data ? flights(this.data) : []; }
  hasBaggage(tarifa: Tarifa) { return tarifa.caracteristicas.some(c => /equipaje|maleta|franquicia/i.test(c.nombre)); }
  personas(idVuelo: number): PasajeroTicket[] { return this.tickets[idVuelo] ?? []; }

  resumenViaje(itinerary: Itinerario): string {
    const orig = itinerary.origen?.codigoIata || '—';
    const dest = itinerary.destino?.codigoIata || '—';
    const esc = this.escalas(itinerary.numeroEscalas);
    if (!itinerary.horaSalida) return `${orig} → ${dest} · ${esc}`;
    const d = new Date(itinerary.horaSalida);
    const fecha = Number.isNaN(d.getTime()) ? '' : d.toLocaleDateString('es-CL', { day: 'numeric', month: 'short' });
    const hora = Number.isNaN(d.getTime()) ? '' : d.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' });
    return `${orig} → ${dest}${fecha ? ' · ' + fecha : ''}${hora ? ' · ' + hora + ' h' : ''} · ${esc}`;
  }

  resumenVuelos(itinerary: Itinerario): string {
    const codigos = itinerary.vuelos.map(x => x.vuelo.numeroVuelo || `#${x.vuelo.idVuelo}`).join(', ');
    const tramos = itinerary.vuelos.reduce((acc, x) => acc + (x.vuelo.segmentos?.length || 0), 0);
    const tramoTxt = tramos > 0 ? `${tramos} ${tramos === 1 ? 'tramo' : 'tramos'}` : `${itinerary.vuelos.length} ${itinerary.vuelos.length === 1 ? 'vuelo' : 'vuelos'}`;
    return `${codigos || 'Sin vuelos'} · ${tramoTxt}`;
  }

  resumenTarifa(itinerary: Itinerario): string {
    const count = itinerary.tarifa.caracteristicas?.length ?? 0;
    return `Tarifa ${itinerary.tarifa.nombre} · ${count} ${count === 1 ? 'condición' : 'condiciones'}`;
  }

  resumenPasajeros(itinerary: Itinerario): string {
    const firstFlight = itinerary.vuelos[0]?.vuelo?.idVuelo;
    const list = firstFlight ? this.personas(firstFlight) : [];
    if (!list.length) return 'Toca para ver pasajeros, asientos y tickets';
    const countTxt = list.length === 1 ? list[0].nombre + ' ' + list[0].apellido : `${list.length} pasajeros`;
    const seats = [...new Set(
      itinerary.vuelos.flatMap(x => this.personas(x.vuelo.idVuelo).map(p => p.asiento?.numeroAsiento).filter(Boolean))
    )];
    const seatTxt = seats.length ? `Asiento ${seats.join(', ')}` : 'Asiento pendiente';
    return `${countTxt} · ${seatTxt}`;
  }

  resumenEquipaje(d: DetalleReserva): string {
    if (!d.equipajes?.length) return 'Sin equipaje adicional registrado';
    const totalKg = d.equipajes.reduce((acc, b) => acc + (b.peso || 0), 0);
    return `${d.equipajes.length} ${d.equipajes.length === 1 ? 'maleta' : 'maletas'} · ${Math.round(totalKg * 10) / 10} kg totales`;
  }

  // ---- Helpers de presentación (no alteran datos ni reglas) ----
  estadoReserva(descripcion: string | null): Etiqueta {
    const texto = descripcion || 'Sin estado';
    if (/cancel/i.test(texto)) return { tono: 'bad', icono: 'close-circle', texto };
    if (/pendiente/i.test(texto)) return { tono: 'warn', icono: 'time-outline', texto };
    if (/confirmad|check-?in/i.test(texto)) return { tono: 'ok', icono: 'checkmark-circle', texto };
    return { tono: 'neutral', icono: 'information-circle', texto };
  }

  estadoVuelo(v: Vuelo): Etiqueta {
    const texto = v.estado.descripcion || v.estado.estado || 'Estado por confirmar';
    const clave = `${v.estado.descripcion ?? ''} ${v.estado.estado ?? ''}`;
    if (/cancel|inactivo/i.test(clave)) return { tono: 'bad', icono: 'close-circle', texto };
    if (/retras|demor/i.test(clave)) return { tono: 'warn', icono: 'alert-circle', texto };
    if (/aterriz|finaliz/i.test(clave)) return { tono: 'neutral', icono: 'checkmark-circle', texto };
    if (/en vuelo/i.test(clave)) return { tono: 'info', icono: 'airplane', texto };
    if (/program|activo/i.test(clave)) return { tono: 'ok', icono: 'checkmark-circle', texto };
    return { tono: 'neutral', icono: 'help-circle', texto };
  }

  escalas(n: number | null): string {
    if (n === null || n === undefined) return 'Escalas por confirmar';
    if (n === 0) return 'Vuelo directo';
    return n === 1 ? '1 escala' : `${n} escalas`;
  }

  clp(monto: number | null): string {
    return monto === null || monto === undefined ? 'Sin especificar' : `$${CLP.format(Math.round(monto))} CLP`;
  }

  iniciales(p: PasajeroTicket): string {
    return `${p.nombre?.trim()[0] ?? ''}${p.apellido?.trim()[0] ?? ''}`.toUpperCase() || '?';
  }

  /** Traduce una característica de tarifa a ícono + texto simple, con unidades. */
  rasgo(c: Caracteristica): Etiqueta {
    const contexto = `${c.nombre} ${c.descripcion ?? ''}`;
    if (c.tipoDato === 'boolean') {
      if (c.valorBool === null) return { tono: 'neutral', icono: 'help-circle', texto: 'Sin especificar' };
      const permiso = /permit/i.test(c.nombre);
      return c.valorBool
        ? { tono: 'ok', icono: 'checkmark-circle', texto: permiso ? 'Permitido' : 'Incluido' }
        : { tono: 'bad', icono: 'close-circle', texto: permiso ? 'No permitido' : 'No incluido' };
    }
    if (c.tipoDato === 'int') {
      const n = c.valorInt;
      if (n === null) return { tono: 'neutral', icono: 'help-circle', texto: 'Sin especificar' };
      if (/hora|plazo|l[ií]mite/i.test(contexto)) {
        const horas = `${n} ${n === 1 ? 'hora' : 'horas'}`;
        return { tono: 'info', icono: 'time-outline', texto: /plazo|l[ií]mite|m[ií]nim|antes|previa/i.test(contexto) ? `Hasta ${horas} antes del vuelo` : horas };
      }
      if (/\bkg\b|peso|kilo/i.test(contexto)) return { tono: 'info', icono: 'briefcase-outline', texto: `${n} kg` };
      return { tono: 'info', icono: 'information-circle', texto: String(n) };
    }
    return { tono: 'info', icono: 'information-circle', texto: featureValue(c) };
  }

  /** Explica en palabras simples por qué el botón de check-in está deshabilitado. */
  motivoCheckin(d: DetalleReserva): string | null {
    if (!d.titular) return 'Solo la persona titular de la reserva puede hacer el check-in.';
    if (d.estadoReserva.descripcion !== 'Confirmada') return 'El check-in estará disponible cuando la reserva esté confirmada.';
    if (d.estadoVentanaCheckin === 'NO_DISPONIBLE_AUN') {
      return d.mensajeCheckin || 'El check-in se habilitará 48 horas antes de la salida del vuelo.';
    }
    if (d.estadoVentanaCheckin === 'CERRADO') {
      return d.mensajeCheckin || 'El check-in en línea cerró 60 minutos antes de la salida del vuelo.';
    }
    return null;
  }

  checkinHabilitado(d: DetalleReserva): boolean {
    if (!d.titular) return false;
    if (d.estadoReserva.descripcion !== 'Confirmada') return false;
    if (d.checkinDisponible !== undefined) return d.checkinDisponible;
    return true;
  }

  descargarPdf(): void {
    if (this.descargandoPdf || !this.data) return;
    this.descargandoPdf = true;
    this.error = '';
    this.api.descargarBoardingPassPdf(this.id).subscribe({
      next: (blob: Blob) => {
        this.descargandoPdf = false;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `BoardingPass_Reserva_${this.id}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.descargandoPdf = false;
        this.error = errorMessage(err);
      }
    });
  }

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
