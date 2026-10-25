import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { DetalleReserva, Notificacion, Pagina, ResumenReserva } from '../../core/models';
import { errorMessage } from '../../core/ui';
import { addIcons } from 'ionicons';
import {
  airplane,
  airplaneOutline,
  alertCircle,
  arrowForwardOutline,
  calendarOutline,
  cashOutline,
  checkmarkCircle,
  closeCircle,
  documentTextOutline,
  enterOutline,
  helpCircle,
  informationCircle,
  locationOutline,
  notificationsOutline,
  peopleOutline,
  personCircleOutline,
  pricetagOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  ticketOutline,
  timeOutline
} from 'ionicons/icons';

addIcons({
  airplane,
  airplaneOutline,
  alertCircle,
  arrowForwardOutline,
  calendarOutline,
  cashOutline,
  checkmarkCircle,
  closeCircle,
  documentTextOutline,
  enterOutline,
  helpCircle,
  informationCircle,
  locationOutline,
  notificationsOutline,
  peopleOutline,
  personCircleOutline,
  pricetagOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  ticketOutline,
  timeOutline
});

export type Tono = 'ok' | 'warn' | 'bad' | 'info' | 'neutral';
export interface Etiqueta { tono: Tono; icono: string; texto: string; }

const CLP_FORMATTER = new Intl.NumberFormat('es-CL', { maximumFractionDigits: 0 });

const CIUDADES_IATA: Record<string, { ciudad: string; aeropuerto: string }> = {
  SCL: { ciudad: 'Santiago', aeropuerto: 'Aeropuerto Arturo Merino Benítez' },
  LIM: { ciudad: 'Lima', aeropuerto: 'Aeropuerto Jorge Chávez' },
  EZE: { ciudad: 'Buenos Aires', aeropuerto: 'Aeropuerto Ministro Pistarini' },
  AEP: { ciudad: 'Buenos Aires', aeropuerto: 'Aeroparque Jorge Newbery' },
  GRU: { ciudad: 'São Paulo', aeropuerto: 'Aeropuerto de Guarulhos' },
  BOG: { ciudad: 'Bogotá', aeropuerto: 'Aeropuerto El Dorado' },
  MIA: { ciudad: 'Miami', aeropuerto: 'Miami International Airport' },
  JFK: { ciudad: 'Nueva York', aeropuerto: 'John F. Kennedy International' },
  MAD: { ciudad: 'Madrid', aeropuerto: 'Aeropuerto Adolfo Suárez Madrid-Barajas' },
  LAX: { ciudad: 'Los Ángeles', aeropuerto: 'Los Angeles International Airport' },
  PEK: { ciudad: 'Beijing', aeropuerto: 'Beijing Capital International' },
  CJC: { ciudad: 'Calama', aeropuerto: 'Aeropuerto El Loa' },
  ANF: { ciudad: 'Antofagasta', aeropuerto: 'Aeropuerto Andrés Sabella' },
  IQQ: { ciudad: 'Iquique', aeropuerto: 'Aeropuerto Diego Aracena' },
  ARI: { ciudad: 'Arica', aeropuerto: 'Aeropuerto Chacalluta' },
  CCP: { ciudad: 'Concepción', aeropuerto: 'Carriel Sur' },
  PMC: { ciudad: 'Puerto Montt', aeropuerto: 'El Tepual' },
  PUQ: { ciudad: 'Punta Arenas', aeropuerto: 'Pdte. Carlos Ibáñez' },
  BBA: { ciudad: 'Balmaceda', aeropuerto: 'Aeródromo Balmaceda' },
  LSC: { ciudad: 'La Serena', aeropuerto: 'Aeródromo La Florida' },
  COR: { ciudad: 'Córdoba', aeropuerto: 'Ing. Ambrosio Taravella' },
  MDZ: { ciudad: 'Mendoza', aeropuerto: 'El Plumerillo' }
};

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

  // Caché de detalles para verificar titularidad y check-in
  private readonly detalleReservaCache = new Map<number, DetalleReserva>();

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
        if (generation === this.generation) {
          this.reservas = data;
          // Pre-cargar detalles para rol y check-in
          for (const r of data.contenido) {
            if (!this.detalleReservaCache.has(r.idReserva)) {
              this.api.reserva(r.idReserva).subscribe({
                next: d => this.detalleReservaCache.set(r.idReserva, d),
                error: () => {}
              });
            }
          }
        }
      }
    } catch (e) {
      if (generation === this.generation) this.error = errorMessage(e);
    } finally {
      if (generation === this.generation) this.busy = false;
    }
  }

  // ---- Helpers de presentación accesibles ----
  clp(monto: number | null | undefined): string {
    if (monto === null || monto === undefined) return 'Sin especificar';
    return `$${CLP_FORMATTER.format(Math.round(monto))} CLP`;
  }

  estadoReserva(descripcion: string | null | undefined): Etiqueta {
    const texto = descripcion || 'Sin estado';
    if (/cancel/i.test(texto)) return { tono: 'bad', icono: 'close-circle', texto };
    if (/pendiente/i.test(texto)) return { tono: 'warn', icono: 'time-outline', texto };
    if (/confirmad|check-?in/i.test(texto)) return { tono: 'ok', icono: 'checkmark-circle', texto };
    if (/finaliz|concluid|aterriz/i.test(texto)) return { tono: 'neutral', icono: 'checkmark-circle', texto: 'Completada' };
    return { tono: 'neutral', icono: 'information-circle', texto };
  }

  ciudadPorIata(iata: string | null | undefined): string {
    if (!iata) return 'Ciudad';
    const code = iata.trim().toUpperCase();
    return CIUDADES_IATA[code]?.ciudad || code;
  }

  esTitular(idReserva: number): boolean | null {
    const d = this.detalleReservaCache.get(idReserva);
    return d ? d.titular : null;
  }

  tieneCheckin(r: ResumenReserva): boolean {
    const d = this.detalleReservaCache.get(r.idReserva);
    if (d?.checkin) return true;
    return /check-?in/i.test(r.estadoReserva?.descripcion ?? '');
  }

  icono(n: Notificacion): string {
    return this.tipoCambio(n).icono;
  }

  tipoCambio(n: Notificacion): { tono: Tono; icono: string; etiqueta: string } {
    const text = `${n.titulo ?? ''} ${n.mensaje ?? ''}`.toLowerCase();
    if (text.includes('cancel')) {
      return { tono: 'bad', icono: 'close-circle', etiqueta: 'Cancelación' };
    }
    if (text.includes('retras') || text.includes('demor') || text.includes('hora')) {
      return { tono: 'warn', icono: 'time-outline', etiqueta: 'Cambio de horario' };
    }
    if (text.includes('puerta') || text.includes('terminal')) {
      return { tono: 'info', icono: 'enter-outline', etiqueta: 'Puerta de embarque' };
    }
    if (text.includes('avión') || text.includes('avion') || text.includes('aeronave')) {
      return { tono: 'info', icono: 'airplane', etiqueta: 'Cambio de avión' };
    }
    return { tono: 'info', icono: 'information-circle', etiqueta: 'Aviso operacional' };
  }

  extraerNumeroVuelo(n: Notificacion): string | null {
    const text = `${n.titulo ?? ''} ${n.mensaje ?? ''}`;
    const match = text.match(/(?:vuelo\s+|SK-?)([A-Z0-9-]{3,7})/i);
    return match ? `Vuelo ${match[1].toUpperCase()}` : null;
  }
}
