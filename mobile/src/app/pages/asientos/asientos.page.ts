import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertController, ToastController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Asiento, MapaAsientos } from '../../core/models';
import { errorMessage } from '../../core/ui';

type CabinKind = 'first' | 'business' | 'economy' | 'other';

interface CabinRow {
  label: string;
  left: Asiento[];
  right: Asiento[];
  kind: CabinKind;
}

interface CabinSection {
  label: string;
  kind: CabinKind;
  rows: CabinRow[];
}

@Component({
  selector: 'app-asientos',
  standalone: false,
  templateUrl: './asientos.page.html',
  styleUrls: ['./asientos.page.scss']
})
export class AsientosPage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly alerts = inject(AlertController);
  private readonly toasts = inject(ToastController);

  // Los nombres de estos parámetros vienen de app-routing.module.ts:
  // /vuelos/reservas/:id/vuelos/:vuelo/asientos
  get id(): number { return Number(this.route.snapshot.paramMap.get('id')); }
  get vuelo(): number { return Number(this.route.snapshot.paramMap.get('vuelo')); }
  get rut(): string | null { return this.route.snapshot.paramMap.get('rut'); }

  data: MapaAsientos | null = null;
  selected: Asiento | null = null;
  sections: CabinSection[] = [];
  busy = false;
  saving = false;
  error = '';
  nombrePasajero = '';

  ionViewWillEnter(): void {
    void this.load();
  }

  private normalize(value: string): string {
    return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }

  private cabinKind(value: string): CabinKind {
    const label = this.normalize(value);
    if (/primera|first|vip/.test(label)) return 'first';
    if (/ejecutiva|business|executive/.test(label)) return 'business';
    if (/economica|economy|turista/.test(label)) return 'economy';
    return 'other';
  }

  private sectionLabel(kind: CabinKind, original: string): string {
    if (kind === 'first') return 'Primera clase';
    if (kind === 'business') return 'Clase ejecutiva';
    if (kind === 'economy') return 'Clase económica';
    return original || 'Cabina';
  }

  // La API retorna el número del asiento, p. ej. 4A o 14F.
  seatLetter(seat: Asiento): string {
    const code = (seat.numeroAsiento ?? '').trim().toUpperCase();
    return code.match(/[A-Z]+$/)?.[0] ?? code;
  }

  private buildCabin(asientos: Asiento[]): CabinSection[] {
    const byRow = new Map<string, Asiento[]>();

    for (const seat of asientos) {
      const code = (seat.numeroAsiento ?? '').trim();
      const row = code.match(/^(\d+)/)?.[1] ?? 'Otros';
      const list = byRow.get(row) ?? [];
      list.push(seat);
      byRow.set(row, list);
    }

    const sortedRows = [...byRow.entries()].sort((a, b) => {
      const difference = Number(a[0]) - Number(b[0]);
      return Number.isNaN(difference) ? a[0].localeCompare(b[0]) : difference;
    });

    const sections: CabinSection[] = [];
    for (const [label, rawSeats] of sortedRows) {
      const seats = [...rawSeats].sort((a, b) =>
        this.seatLetter(a).localeCompare(this.seatLetter(b), 'es', { numeric: true })
      );
      const originalClass = seats.find(s => !!s.clase)?.clase?.trim() ?? '';
      const kind = this.cabinKind(originalClass);

      // A/B/C a la izquierda, D/E/F a la derecha del pasillo.
      // Solo se dibujan los asientos existentes: no inventamos B/E en filas 2+2.
      let left = seats.filter(s => /^[ABC]$/.test(this.seatLetter(s)));
      let right = seats.filter(s => /^[DEF]$/.test(this.seatLetter(s)));
      if (left.length + right.length !== seats.length || !left.length || !right.length) {
        // Distribución alternativa: mostramos todos los asientos sin descartarlos.
        const split = Math.ceil(seats.length / 2);
        left = seats.slice(0, split);
        right = seats.slice(split);
      }

      const row: CabinRow = { label, left, right, kind };
      const last = sections[sections.length - 1];
      if (last && last.kind === kind &&
          (kind !== 'other' || last.label === this.sectionLabel(kind, originalClass))) {
        last.rows.push(row);
      } else {
        sections.push({
          label: this.sectionLabel(kind, originalClass),
          kind,
          rows: [row]
        });
      }
    }
    return sections;
  }

  private setData(map: MapaAsientos): void {
    this.data = map;
    this.selected = null;
    this.sections = this.buildCabin(map.asientos ?? []);
  }

  isCurrent(seat: Asiento): boolean {
    return seat.estado === 'seleccionado' ||
      (!!this.data?.asientoActual && this.data.asientoActual.idAsiento === seat.idAsiento);
  }

  isPicked(seat: Asiento): boolean {
    return this.selected?.idAsiento === seat.idAsiento;
  }

  isAvailable(seat: Asiento): boolean {
    return seat.seleccionable && !this.isCurrent(seat) && !this.isPicked(seat);
  }

  async load(): Promise<void> {
    this.busy = true;
    this.error = '';
    this.data = null;
    this.selected = null;
    this.sections = [];
    try {
      this.setData(await firstValueFrom(
        this.rut
          ? this.api.asientosPasajero(this.id, this.vuelo, this.rut)
          : this.api.asientos(this.id, this.vuelo)
      ));
      if (this.rut) {
        try {
          const personas = await firstValueFrom(this.api.ticketsReserva(this.id, this.vuelo));
          const persona = personas.find(p => p.rut === this.rut);
          this.nombrePasajero = persona ? `${persona.nombre} ${persona.apellido}` : '';
        } catch {
          this.nombrePasajero = '';
        }
      }
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }

  choose(seat: Asiento): void {
    if (!seat.seleccionable || this.saving) return;
    this.selected = this.isPicked(seat) ? null : seat;
  }

  async save(): Promise<void> {
    if (!this.selected || this.saving || !this.data) return;
    const seat = this.selected;
    const isInitial = !this.data.asientoActual;
    const confirmation = await this.alerts.create({
      header: isInitial ? `Elegir asiento ${seat.numeroAsiento}` : `Cambiar al asiento ${seat.numeroAsiento}`,
      message: isInitial
        ? `Se asignará el asiento ${seat.numeroAsiento} (${seat.clase ?? 'clase por confirmar'}) a este vuelo.`
        : 'El cambio conserva las condiciones de la clase de tu asiento actual.',
      buttons: [
        { text: 'Volver', role: 'cancel' },
        { text: 'Confirmar', role: 'confirm' }
      ]
    });
    await confirmation.present();
    if ((await confirmation.onDidDismiss()).role !== 'confirm') return;

    this.saving = true;
    this.error = '';
    try {
      this.setData(await firstValueFrom(
        this.rut
          ? this.api.cambiarAsientoPasajero(this.id, this.vuelo, this.rut, seat.idAsiento)
          : this.api.cambiarAsiento(this.id, this.vuelo, seat.idAsiento)
      ));
      const toast = await this.toasts.create({
        message: isInitial ? 'Asiento asignado correctamente.' : 'Asiento actualizado correctamente.',
        duration: 2500,
        color: 'success'
      });
      await toast.present();
    } catch (e) {
      const message = errorMessage(e);
      try {
        this.setData(await firstValueFrom(
        this.rut
          ? this.api.asientosPasajero(this.id, this.vuelo, this.rut)
          : this.api.asientos(this.id, this.vuelo)
      ));
      if (this.rut) {
        try {
          const personas = await firstValueFrom(this.api.ticketsReserva(this.id, this.vuelo));
          const persona = personas.find(p => p.rut === this.rut);
          this.nombrePasajero = persona ? `${persona.nombre} ${persona.apellido}` : '';
        } catch {
          this.nombrePasajero = '';
        }
      }
      } catch {
        this.selected = null;
      }
      this.error = message;
    } finally {
      this.saving = false;
    }
  }

  trackSeat(_index: number, seat: Asiento): number {
    return seat.idAsiento;
  }
}
