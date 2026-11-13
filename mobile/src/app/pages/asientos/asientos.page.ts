import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertController, ToastController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Asiento, ConfiguracionCabinaItem, MapaAsientos, Vuelo } from '../../core/models';
import { errorMessage } from '../../core/ui';
import { addIcons } from 'ionicons';
import {
  airplane,
  airplaneOutline,
  alertCircle,
  arrowBack,
  arrowForward,
  briefcaseOutline,
  cardOutline,
  cashOutline,
  checkmark,
  checkmarkCircle,
  chevronDownOutline,
  close,
  closeCircle,
  helpCircle,
  informationCircle,
  locationOutline,
  lockClosedOutline,
  peopleOutline,
  personCircleOutline,
  personOutline,
  pricetagOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  star,
  timeOutline,
  trendingUpOutline,
  walletOutline
} from 'ionicons/icons';

// Registrar íconos necesarios para la pantalla de selección de asientos
addIcons({
  airplane,
  airplaneOutline,
  alertCircle,
  arrowBack,
  arrowForward,
  briefcaseOutline,
  cardOutline,
  cashOutline,
  checkmark,
  checkmarkCircle,
  chevronDownOutline,
  close,
  closeCircle,
  helpCircle,
  informationCircle,
  locationOutline,
  lockClosedOutline,
  peopleOutline,
  personCircleOutline,
  personOutline,
  pricetagOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  star,
  timeOutline,
  trendingUpOutline,
  walletOutline
});

export type CabinKind = 'first' | 'business' | 'economy' | 'other';

export interface CabinBlock {
  asientos: Asiento[];
}

export interface CabinRow {
  numeroFila: number;
  label: string;
  clase: string;
  kind: CabinKind;
  esEmergencia: boolean;
  precio?: number | null;
  bloques: CabinBlock[];
}

export interface CabinSection {
  label: string;
  claseOriginal: string;
  kind: CabinKind;
  precio?: number | null;
  bloquesLetras: string[][];
  rows: CabinRow[];
}

const CLP = new Intl.NumberFormat('es-CL', { maximumFractionDigits: 0 });

@Component({
  selector: 'app-asientos',
  standalone: false,
  templateUrl: './asientos.page.html',
  styleUrls: ['./asientos.page.scss']
})
export class AsientosPage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly alerts = inject(AlertController);
  private readonly toasts = inject(ToastController);

  // Parámetros de ruta:
  // /vuelos/reservas/:id/vuelos/:vuelo/asientos
  // /vuelos/reservas/:id/vuelos/:vuelo/tickets/:rut/asientos
  get id(): number { return Number(this.route.snapshot.paramMap.get('id')); }
  get vuelo(): number { return Number(this.route.snapshot.paramMap.get('vuelo')); }
  get rut(): string | null { return this.route.snapshot.paramMap.get('rut'); }

  data: MapaAsientos | null = null;
  vueloInfo: Vuelo | null = null;
  selected: Asiento | null = null;
  sections: CabinSection[] = [];
  busy = false;
  saving = false;
  error = '';
  nombrePasajero = '';
  metodoPago = 1; // 1: Webpay Plus / Tarjetas, 2: PayPal
  showGuide = false;
  showUpgradeBreakdown = false;

  toggleGuide(): void {
    this.showGuide = !this.showGuide;
  }

  toggleUpgradeBreakdown(): void {
    this.showUpgradeBreakdown = !this.showUpgradeBreakdown;
  }

  get vueloLabel(): string {
    if (this.vueloInfo?.numeroVuelo) return `Vuelo ${this.vueloInfo.numeroVuelo}`;
    return `Vuelo #${this.vuelo}`;
  }

  get rutaLabel(): string {
    if (!this.vueloInfo?.segmentos?.length) return '';
    const orig = this.vueloInfo.segmentos[0]?.origen?.codigoIata;
    const dest = this.vueloInfo.segmentos[this.vueloInfo.segmentos.length - 1]?.destino?.codigoIata;
    if (orig && dest) return `${orig} → ${dest}`;
    return '';
  }

  get origenCiudad(): string {
    return this.vueloInfo?.segmentos?.[0]?.origen?.ciudad || '';
  }

  get destinoCiudad(): string {
    const s = this.vueloInfo?.segmentos;
    return s && s.length ? s[s.length - 1]?.destino?.ciudad || '' : '';
  }

  get claseContratada(): string {
    return this.data?.asientoActual?.clase || 'Clase por confirmar';
  }

  clp(monto: number | null): string {
    return monto === null || monto === undefined ? 'Sin especificar' : `$${CLP.format(Math.round(monto))} CLP`;
  }

  async ionViewWillEnter(): Promise<void> {
    const pasarela = this.route.snapshot.queryParamMap.get('pasarela');
    const orden = this.route.snapshot.queryParamMap.get('orden');
    const cancelado = this.route.snapshot.queryParamMap.get('cancelado') === 'true' ||
                      this.route.snapshot.queryParamMap.get('TBK_TOKEN') != null;
    const token = this.route.snapshot.queryParamMap.get('token_ws') ||
                  this.route.snapshot.queryParamMap.get('token');

    if (orden && (cancelado || token)) {
      await this.procesarRetornoPasarela(orden, pasarela || 'webpay', token, cancelado);
    } else {
      await this.load();
    }
  }

  private async procesarRetornoPasarela(orden: string, pasarela: string, token: string | null, cancelado: boolean): Promise<void> {
    let pendingInfo: any = null;
    try {
      const raw = sessionStorage.getItem('skyway_mobile_seat_pending');
      if (raw) pendingInfo = JSON.parse(raw);
    } catch {}

    // Limpiar query params de la URL para evitar reprocesos al refrescar
    void this.router.navigate([], { replaceUrl: true, queryParams: {} });

    if (cancelado || !token) {
      try { sessionStorage.removeItem('skyway_mobile_seat_pending'); } catch {}
      await this.load();
      const alert = await this.alerts.create({
        header: 'Pago Cancelado',
        message: 'Has cancelado el proceso de pago en la pasarela. No se efectuaron cobros ni cambios en tu asiento.',
        buttons: ['Entendido']
      });
      await alert.present();
      return;
    }

    this.busy = true;
    this.error = '';
    try {
      const mapa = await firstValueFrom(this.api.confirmarPagoCambioAsiento(orden, pasarela, token));
      try { sessionStorage.removeItem('skyway_mobile_seat_pending'); } catch {}
      this.setData(mapa);
      const nombreMetodo = pasarela === 'paypal' ? 'PayPal' : 'Webpay Plus';
      const montoTexto = pendingInfo?.totalPagar ? this.clp(pendingInfo.totalPagar) : '';
      const toast = await this.toasts.create({
        message: `¡Ascenso confirmado! Se procesó el pago ${montoTexto ? 'de ' + montoTexto + ' ' : ''}vía ${nombreMetodo}.`,
        duration: 4000,
        color: 'success'
      });
      await toast.present();
    } catch (e) {
      try { sessionStorage.removeItem('skyway_mobile_seat_pending'); } catch {}
      this.error = errorMessage(e);
      await this.load();
    } finally {
      this.busy = false;
    }
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

  // La API retorna el número del asiento, p. ej. 4A o 14F, o campos separados fila/letra.
  seatLetter(seat: Asiento): string {
    if (seat.letra && seat.letra.trim().length > 0) {
      return seat.letra.trim().toUpperCase();
    }
    const code = (seat.numeroAsiento ?? '').trim().toUpperCase();
    return code.match(/[A-Z]+$/)?.[0] ?? code;
  }

  private extraerFila(numeroAsiento: string): number {
    const d = (numeroAsiento || '').replace(/[^0-9]/g, '');
    return d ? parseInt(d, 10) : 1;
  }

  private extraerLetra(numeroAsiento: string): string {
    const l = (numeroAsiento || '').replace(/[0-9]/g, '').trim().toUpperCase();
    return l || numeroAsiento || '?';
  }

  private parsearDistribucionColumnas(distribucion: string | null | undefined, totalFila: number): number[] {
    const resultado: number[] = [];
    if (distribucion && distribucion.includes('-')) {
      const partes = distribucion.split('-');
      for (const p of partes) {
        const val = parseInt(p.trim(), 10);
        if (!isNaN(val) && val > 0) resultado.push(val);
      }
      if (resultado.length > 0) return resultado;
    }

    // Fallbacks inteligentes por cantidad de asientos en la fila (idéntico a la Web)
    if (totalFila === 4) return [2, 2];
    if (totalFila === 6) return [3, 3];
    if (totalFila === 8) return [2, 4, 2];
    if (totalFila === 9) return [3, 3, 3];
    if (totalFila === 10) return [3, 4, 3];
    if (totalFila === 3) return [1, 2];
    if (totalFila === 5) return [2, 3];
    if (totalFila === 7) return [2, 3, 2];
    return [totalFila];
  }

  private buildCabin(asientos: Asiento[], configs: ConfiguracionCabinaItem[] = []): CabinSection[] {
    if (!asientos || asientos.length === 0) return [];

    // 1. Agrupar por fila numérica
    const mapaFilas = new Map<number, Asiento[]>();
    for (const a of asientos) {
      const numFila = (a.fila && a.fila > 0) ? a.fila : this.extraerFila(a.numeroAsiento);
      if (!a.letra || !a.letra.trim()) {
        a.letra = this.extraerLetra(a.numeroAsiento);
      }
      const list = mapaFilas.get(numFila) ?? [];
      list.push(a);
      mapaFilas.set(numFila, list);
    }

    // 2. Ordenar las filas numéricamente ascendente (1, 2, 3...)
    const filasOrdenadas = [...mapaFilas.entries()].sort((a, b) => a[0] - b[0]);

    const rowsCabina: CabinRow[] = [];

    for (const [numFila, asientosFilaRaw] of filasOrdenadas) {
      // 3. Ordenar asientos dentro de la fila por letra alfabéticamente (A, B, C...)
      const asientosFila = [...asientosFilaRaw].sort((a, b) => {
        const la = (a.letra ?? this.extraerLetra(a.numeroAsiento)).toUpperCase();
        const lb = (b.letra ?? this.extraerLetra(b.numeroAsiento)).toUpperCase();
        return la.localeCompare(lb, 'es', { numeric: true });
      });

      // 4. Buscar configuración de cabina correspondiente para esta fila
      const configFila = configs.find(cc =>
        cc.filaInicio != null && cc.filaFin != null &&
        numFila >= cc.filaInicio && numFila <= cc.filaFin
      );

      const claseFila = configFila?.clase || asientosFila[0]?.clase || 'Económica';
      const kind = this.cabinKind(claseFila);
      const esEmergencia = Boolean(
        configFila?.esSalidaEmergencia ||
        asientosFila.some(a => a.esEmergencia)
      );

      // 5. Agrupar bloques según Distribucion_Columnas (o fallback de pasillos)
      const tamaniosBloques = this.parsearDistribucionColumnas(
        configFila?.distribucionColumnas,
        asientosFila.length
      );

      const bloques: CabinBlock[] = [];
      let indice = 0;
      const totalAsientos = asientosFila.length;

      for (const tamBloque of tamaniosBloques) {
        if (indice >= totalAsientos) break;
        const bloqueAsientos: Asiento[] = [];
        for (let b = 0; b < tamBloque && indice < totalAsientos; b++) {
          bloqueAsientos.push(asientosFila[indice++]);
        }
        if (bloqueAsientos.length > 0) {
          bloques.push({ asientos: bloqueAsientos });
        }
      }

      // Respaldo si sobran asientos
      if (indice < totalAsientos) {
        if (bloques.length === 0) bloques.push({ asientos: [] });
        while (indice < totalAsientos) {
          bloques[bloques.length - 1].asientos.push(asientosFila[indice++]);
        }
      }

      rowsCabina.push({
        numeroFila: numFila,
        label: String(numFila),
        clase: claseFila,
        kind,
        esEmergencia,
        precio: asientosFila[0]?.precio,
        bloques
      });
    }

    // 6. Agrupar filas en Secciones de Clase consecutivas
    const sections: CabinSection[] = [];
    for (const row of rowsCabina) {
      const lastSection = sections[sections.length - 1];
      if (lastSection && lastSection.kind === row.kind) {
        lastSection.rows.push(row);
      } else {
        // Calcular letras de cabecera para los bloques a partir de esta primera fila de la clase
        const bloquesLetras: string[][] = row.bloques.map(b =>
          b.asientos.map(a => (a.letra ?? this.extraerLetra(a.numeroAsiento)).toUpperCase())
        );

        sections.push({
          label: this.sectionLabel(row.kind, row.clase),
          claseOriginal: row.clase,
          kind: row.kind,
          precio: row.precio,
          bloquesLetras,
          rows: [row]
        });
      }
    }

    return sections;
  }

  private setData(map: MapaAsientos): void {
    this.data = map;
    this.selected = null;
    this.sections = this.buildCabin(map.asientos ?? [], map.configuracionesCabina ?? []);
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
      const mapaPromise = firstValueFrom(
        this.rut
          ? this.api.asientosPasajero(this.id, this.vuelo, this.rut)
          : this.api.asientos(this.id, this.vuelo)
      );
      const vueloPromise = firstValueFrom(this.api.vuelo(this.id, this.vuelo)).catch(() => null);

      const [mapa, vuelo] = await Promise.all([mapaPromise, vueloPromise]);
      this.vueloInfo = vuelo;
      this.setData(mapa);

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

  cancelSelection(): void {
    this.selected = null;
  }

  setMetodoPago(metodo: number): void {
    this.metodoPago = metodo;
  }

  isUpgrade(seat: Asiento): boolean {
    return Boolean(seat.esUpgrade && (seat.totalPagar ?? 0) > 0);
  }

  async save(): Promise<void> {
    if (!this.selected || this.saving || !this.data) return;
    const seat = this.selected;
    const isInitial = !this.data.asientoActual;
    const upgrade = this.isUpgrade(seat);
    const totalPagar = seat.totalPagar ?? 0;
    const diffTarifa = seat.diferenciaPrecio ?? 0;
    const cargoGestion = seat.cargoGestion ?? 0;
    const metodoNombre = this.metodoPago === 2 ? 'PayPal' : 'Webpay Plus / Tarjeta';

    let msg = '';
    if (isInitial) {
      msg = `Se asignará el asiento ${seat.numeroAsiento} (${seat.clase ?? 'clase por confirmar'}) a este vuelo.`;
    } else if (upgrade) {
      msg = `Has seleccionado un ascenso a ${seat.clase ?? 'Clase Superior'}.\n\n` +
            `• Diferencia de tarifa: ${this.clp(diffTarifa)}\n` +
            `• Cargo de gestión: ${this.clp(cargoGestion)}\n` +
            `• Total a pagar: ${this.clp(totalPagar)}\n\n` +
            `Método de pago: ${metodoNombre}.\n` +
            `¿Deseas confirmar y procesar el pago en la pasarela?`;
    } else {
      msg = `El cambio conservará las condiciones de tu pasaje (${this.claseContratada}) sin costo adicional.`;
    }

    const confirmation = await this.alerts.create({
      header: isInitial
        ? `Elegir asiento ${seat.numeroAsiento}`
        : upgrade
          ? `Ascenso al asiento ${seat.numeroAsiento}`
          : `Cambiar al asiento ${seat.numeroAsiento}`,
      message: msg,
      buttons: [
        { text: 'Volver', role: 'cancel' },
        { text: upgrade ? `Pagar y confirmar (${this.clp(totalPagar)})` : 'Confirmar', role: 'confirm' }
      ]
    });
    await confirmation.present();
    if ((await confirmation.onDidDismiss()).role !== 'confirm') return;

    this.saving = true;
    this.error = '';
    try {
      const urlBase = typeof window !== 'undefined' ? window.location.origin : 'http://localhost:8101';
      const res = await firstValueFrom(
        this.rut
          ? this.api.cambiarAsientoPasajero(this.id, this.vuelo, this.rut, seat.idAsiento, upgrade ? this.metodoPago : 1, urlBase)
          : this.api.cambiarAsiento(this.id, this.vuelo, seat.idAsiento, upgrade ? this.metodoPago : 1, urlBase)
      );

      // Si requiere pago en pasarela externa (Webpay Plus o PayPal)
      if (res?.redirectUrl && typeof window !== 'undefined') {
        try {
          sessionStorage.setItem('skyway_mobile_seat_pending', JSON.stringify({
            ordenCompra: res.ordenCompra,
            pasarela: this.metodoPago === 2 ? 'paypal' : 'webpay',
            idReserva: this.id,
            idVuelo: this.vuelo,
            rut: this.rut,
            numeroAsiento: seat.numeroAsiento,
            clase: seat.clase,
            totalPagar,
            metodoNombre
          }));
        } catch {}
        window.location.href = res.redirectUrl;
        return;
      }

      this.setData(res);
      const toast = await this.toasts.create({
        message: isInitial
          ? 'Asiento asignado correctamente.'
          : 'Asiento actualizado correctamente sin costo adicional.',
        duration: 3000,
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
