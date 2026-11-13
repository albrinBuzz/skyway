import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Capacitor } from '@capacitor/core';
import { firstValueFrom } from 'rxjs';
import { addIcons } from 'ionicons';
import {
  addOutline,
  airplane,
  airplaneOutline,
  alertCircle,
  alertCircleOutline,
  arrowBackOutline,
  arrowForwardOutline,
  bagHandleOutline,
  briefcaseOutline,
  calendarOutline,
  cardOutline,
  cashOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  checkmarkDoneOutline,
  chevronBackOutline,
  chevronDownOutline,
  chevronForwardOutline,
  closeCircleOutline,
  closeOutline,
  cubeOutline,
  ellipseOutline,
  informationCircle,
  informationCircleOutline,
  layersOutline,
  locationOutline,
  personOutline,
  pricetagOutline,
  radioButtonOn,
  radioOutline,
  receiptOutline,
  refreshOutline,
  removeOutline,
  search,
  searchOutline,
  shieldCheckmarkOutline,
  sparklesOutline,
  star,
  swapHorizontalOutline,
  ticketOutline,
  time,
  timeOutline,
  trashOutline,
  walletOutline
} from 'ionicons/icons';
import { ApiService } from '../../core/api.service';
import {
  AeropuertoCatalogo,
  AsientoSeleccionadoSolicitud,
  AsientoVueloDisponible,
  ConfiguracionCabinaItem,
  ItemEquipajeSolicitud,
  ItinerarioDisponible,
  ReservaCreada,
  TarifaDisponible,
  VueloConAsientos
} from '../../core/models';
import { errorMessage } from '../../core/ui';

addIcons({
  addOutline,
  airplane,
  airplaneOutline,
  alertCircle,
  alertCircleOutline,
  arrowBackOutline,
  arrowForwardOutline,
  bagHandleOutline,
  briefcaseOutline,
  calendarOutline,
  cardOutline,
  cashOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  checkmarkDoneOutline,
  chevronBackOutline,
  chevronDownOutline,
  chevronForwardOutline,
  closeCircleOutline,
  closeOutline,
  cubeOutline,
  ellipseOutline,
  informationCircle,
  informationCircleOutline,
  layersOutline,
  locationOutline,
  personOutline,
  pricetagOutline,
  radioButtonOn,
  radioOutline,
  receiptOutline,
  refreshOutline,
  removeOutline,
  search,
  searchOutline,
  shieldCheckmarkOutline,
  sparklesOutline,
  star,
  swapHorizontalOutline,
  ticketOutline,
  time,
  timeOutline,
  trashOutline,
  walletOutline
});

export interface FilaAsientosView {
  numeroFila: number;
  clase: string;
  esPrimeraDeClase: boolean;
  esEmergencia: boolean;
  precio: number;
  bloques: AsientoVueloDisponible[][];
}

const CLP = new Intl.NumberFormat('es-CL', { maximumFractionDigits: 0 });

@Component({
  selector: 'app-inicio',
  standalone: false,
  templateUrl: './inicio.page.html',
  styleUrls: ['./inicio.page.scss']
})
export class InicioPage {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  aeropuertos: AeropuertoCatalogo[] = [];
  aeropuertosFiltrados: AeropuertoCatalogo[] = [];
  selectorAbierto = false;
  campoSelector: 'origen' | 'destino' | null = null;
  codigoSeleccionado = '';
  textoAeropuerto = '';
  resultados: ItinerarioDisponible[] = [];

  // Stepper: 1: Vuelo, 2: Tarifa, 3: Asiento, 4: Equipaje, 5: Pago
  pasoActual = 1;
  itinerarioSeleccionado: ItinerarioDisponible | null = null;
  tarifaSeleccionada: TarifaDisponible | null = null;
  vuelosItinerario: VueloConAsientos[] = [];
  filasActivas: FilaAsientosView[] = [];
  vueloActivoIndex = 0;
  cargandoAsientos = false;
  asientosSeleccionadosPorVuelo: { [idVuelo: number]: AsientoVueloDisponible } = {};

  cantCabina = 0;
  cantBodega15 = 0;
  cantBodega23 = 0;
  metodoPago = 1; // 1: Webpay Plus / Tarjetas, 2: PayPal
  procesandoPago = false;
  reservaConfirmada: ReservaCreada | null = null;
  cargandoCatalogo = false;
  buscando = false;
  busquedaRealizada = false;
  error = '';

  // Desplegables de UI
  expandedTarifas = new Set<string>();
  showCheckoutDetail = false;

  isTarifaOpen(nombre: string): boolean {
    return this.expandedTarifas.has(nombre);
  }

  toggleTarifa(nombre: string): void {
    if (this.expandedTarifas.has(nombre)) {
      this.expandedTarifas.delete(nombre);
    } else {
      this.expandedTarifas.add(nombre);
    }
  }

  readonly hoy = this.fechaLocal(new Date());

  readonly form = this.fb.nonNullable.group({
    origen: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
    destino: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
    fecha: ['']
  });

  get tieneSeleccionOBusqueda(): boolean {
    return Boolean(
      this.form.controls.origen.value ||
      this.form.controls.destino.value ||
      this.form.controls.fecha.value ||
      this.resultados.length ||
      this.busquedaRealizada ||
      this.error
    );
  }

  limpiarBusqueda(): void {
    this.form.patchValue({ origen: '', destino: '', fecha: '' });
    this.form.markAsPristine();
    this.form.markAsUntouched();
    this.form.controls.origen.markAsPristine();
    this.form.controls.origen.markAsUntouched();
    this.form.controls.destino.markAsPristine();
    this.form.controls.destino.markAsUntouched();
    this.form.controls.fecha.markAsPristine();
    this.form.controls.fecha.markAsUntouched();
    this.resultados = [];


    this.busquedaRealizada = false;
    this.error = '';
    this.codigoSeleccionado = '';
    this.itinerarioSeleccionado = null;
    this.tarifaSeleccionada = null;
    this.vuelosItinerario = [];
    this.filasActivas = [];
    this.vueloActivoIndex = 0;
    this.asientosSeleccionadosPorVuelo = {};
    this.pasoActual = 1;
    this.reservaConfirmada = null;
    this.cantCabina = 0;
    this.cantBodega15 = 0;
    this.cantBodega23 = 0;
    this.metodoPago = 1;
  }

  limpiarCampo(campo: 'origen' | 'destino', event?: Event): void {
    event?.stopPropagation();
    event?.preventDefault();
    this.form.controls[campo].setValue('');
    this.form.controls[campo].markAsUntouched();
    this.form.controls[campo].markAsPristine();
    this.form.markAsUntouched();
    this.form.markAsPristine();
    if (this.campoSelector === campo) {
      this.codigoSeleccionado = '';
    this.itinerarioSeleccionado = null;
    }
    this.resultados = [];


    this.busquedaRealizada = false;
    this.error = '';
  }

  limpiarFecha(event?: Event): void {
    event?.stopPropagation();
    event?.preventDefault();
    this.form.controls.fecha.setValue('');
    this.form.controls.fecha.markAsUntouched();
    this.form.controls.fecha.markAsPristine();
    this.resultados = [];
    this.itinerarioSeleccionado = null;


    this.busquedaRealizada = false;
    this.error = '';
  }

  quitarSeleccionModal(): void {
    if (this.campoSelector) {
      this.limpiarCampo(this.campoSelector);
    }
    this.codigoSeleccionado = '';
    this.itinerarioSeleccionado = null;
    this.cerrarSelector();
  }

  ionViewWillEnter() {
    if (!this.aeropuertos.length && !this.cargandoCatalogo) void this.cargarAeropuertos();
    void this.verificarRetornoPasarelaPago();
  }

  private async verificarRetornoPasarelaPago(): Promise<void> {
    if (typeof window === 'undefined' || !window.location.search) return;
    const normalizedSearch = window.location.search.replace(/\?/g, '&').replace(/^&/, '?');
    const params = new URLSearchParams(normalizedSearch);
    const pasarela = params.get('pasarela');
    const orden = params.get('orden');
    if (!pasarela || !orden) return;

    const token = params.get('token_ws') || params.get('token');
    const tbkToken = params.get('TBK_TOKEN');
    const cancelado = params.get('cancelado') === 'true' || Boolean(tbkToken);

    window.history.replaceState({}, document.title, window.location.pathname);

    let reservaPayload: any = undefined;
    try {
      const raw = sessionStorage.getItem('skyway_mobile_checkout_pending');
      if (raw) {
        const snap = JSON.parse(raw);
        if (snap.formValues) {
          this.form.patchValue(snap.formValues);
        }
        this.resultados = snap.resultados || [];
        this.busquedaRealizada = Boolean(this.resultados.length);
        this.itinerarioSeleccionado = snap.itinerarioSeleccionado || null;
        this.tarifaSeleccionada = snap.tarifaSeleccionada || null;
        this.vuelosItinerario = snap.vuelosItinerario || [];
        this.asientosSeleccionadosPorVuelo = snap.asientosSeleccionadosPorVuelo || {};
        this.cantCabina = snap.cantCabina || 0;
        this.cantBodega15 = snap.cantBodega15 || 0;
        this.cantBodega23 = snap.cantBodega23 || 0;
        this.metodoPago = snap.metodoPago || 1;
        reservaPayload = snap.reservaPayload;
        this.recalcularFilasVueloActivo();
      }
    } catch {
      // Ignorar errores de parseo de sessionStorage
    }

    if (cancelado || !token) {
      this.pasoActual = 5;
      this.error = 'Has cancelado el pago en la pasarela. Puedes intentarlo nuevamente o elegir otro método de pago.';
      return;
    }

    this.pasoActual = 5;
    this.procesandoPago = true;
    this.error = '';
    try {
      const res = await firstValueFrom(
        this.api.confirmarPagoReserva(orden, pasarela, token, reservaPayload)
      );
      try {
        sessionStorage.removeItem('skyway_mobile_checkout_pending');
      } catch {
        // Ignorar
      }
      this.reservaConfirmada = res;
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.procesandoPago = false;
    }
  }

  private fechaLocal(fecha: Date): string {
    const y = fecha.getFullYear();
    const m = String(fecha.getMonth() + 1).padStart(2, '0');
    const d = String(fecha.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  async cargarAeropuertos() {
    this.cargandoCatalogo = true;
    this.error = '';
    try {
      this.aeropuertos = await firstValueFrom(this.api.aeropuertos());
      this.actualizarListaAeropuertos();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.cargandoCatalogo = false;
    }
  }

  /** Muestra una ventana con buscador y selección única tipo radio. */
  abrirSelector(campo: 'origen' | 'destino'): void {
    if (this.cargandoCatalogo) return;
    this.campoSelector = campo;
    this.codigoSeleccionado = this.form.controls[campo].value;
    this.textoAeropuerto = '';
    this.actualizarListaAeropuertos();
    this.selectorAbierto = true;
  }

  cerrarSelector(): void {
    this.selectorAbierto = false;
    this.campoSelector = null;
    this.textoAeropuerto = '';
  }

  actualizarFiltroAeropuerto(evento: Event): void {
    const detalle = (evento as CustomEvent<{ value?: string | null }>).detail;
    this.textoAeropuerto = detalle?.value ?? '';
    this.actualizarListaAeropuertos();
  }

  limpiarFiltroAeropuerto(): void {
    this.textoAeropuerto = '';
    this.actualizarListaAeropuertos();
  }

  /** Ignora mayúsculas, tildes y espacios adicionales en la búsqueda. */
  private textoNormalizado(texto: string): string {
    return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('es').trim();
  }

  private actualizarListaAeropuertos(): void {
    const palabras = this.textoNormalizado(this.textoAeropuerto).split(/\s+/).filter(Boolean);
    this.aeropuertosFiltrados = this.aeropuertos
      .filter(a => {
        const campos = this.textoNormalizado(
          `${a.codigoIata} ${a.ciudad ?? ''} ${a.nombreAeropuerto} ${a.pais ?? ''}`
        );
        return palabras.every(palabra => campos.includes(palabra));
      })
      .sort((a, b) => (a.ciudad || a.nombreAeropuerto).localeCompare(
        b.ciudad || b.nombreAeropuerto, 'es', { sensitivity: 'base' }
      ));
  }

  seleccionarDesdeRadio(evento: Event): void {
    const valor = (evento as CustomEvent<{ value?: string | null }>).detail?.value;
    this.seleccionarAeropuerto(valor);
  }

  seleccionarAeropuerto(codigo: string | null | undefined): void {
    if (!this.selectorAbierto || !this.campoSelector || !codigo) return;
    const campo = this.campoSelector;
    const seleccionado = this.aeropuertos.find(a => a.codigoIata === codigo);
    if (!seleccionado) return;
    this.form.controls[campo].setValue(seleccionado.codigoIata);
    this.form.controls[campo].markAsTouched();
    this.codigoSeleccionado = seleccionado.codigoIata;
    this.error = '';
    this.cerrarSelector();
  }

  etiquetaSeleccionada(codigo: string): string {
    if (!codigo) return 'Seleccionar aeropuerto';
    const aeropuerto = this.aeropuertos.find(a => a.codigoIata === codigo);
    return aeropuerto ? `${aeropuerto.codigoIata} · ${aeropuerto.ciudad || aeropuerto.nombreAeropuerto}` : codigo;
  }

  nombreSeleccionado(codigo: string): string {
    return this.aeropuertos.find(a => a.codigoIata === codigo)?.nombreAeropuerto ?? '';
  }

  aeropuertoPorCodigo(codigo: string | null | undefined): AeropuertoCatalogo | undefined {
    if (!codigo) return undefined;
    return this.aeropuertos.find(a => a.codigoIata.toUpperCase() === codigo.toUpperCase());
  }

  ciudadODefecto(codigo: string | null | undefined): string {
    const a = this.aeropuertoPorCodigo(codigo);
    return a?.ciudad || a?.nombreAeropuerto || codigo || 'Por confirmar';
  }

  nombreAeropuertoODefecto(codigo: string | null | undefined): string {
    const a = this.aeropuertoPorCodigo(codigo);
    return a?.nombreAeropuerto || 'Aeropuerto por confirmar';
  }

  paisODefecto(codigo: string | null | undefined): string {
    const a = this.aeropuertoPorCodigo(codigo);
    return a?.pais || 'Chile';
  }

  clp(monto: number | null | undefined): string {
    if (monto === null || monto === undefined) return 'Sin especificar';
    return `$${CLP.format(Math.round(monto))} CLP`;
  }

  escalasTexto(n: number | null | undefined): string {
    if (n === null || n === undefined) return 'Escalas por confirmar';
    if (n === 0) return 'Vuelo directo';
    return n === 1 ? '1 escala' : `${n} escalas`;
  }

  fechaFormateada(fechaIso: string | null | undefined): string {
    if (!fechaIso) return 'Por confirmar';
    try {
      const parts = fechaIso.split('T')[0].split('-');
      if (parts.length === 3) {
        const d = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
        return d.toLocaleDateString('es-CL', {
          weekday: 'long',
          day: 'numeric',
          month: 'long'
        });
      }
      const d = new Date(fechaIso);
      return d.toLocaleDateString('es-CL', {
        weekday: 'long',
        day: 'numeric',
        month: 'long'
      });
    } catch {
      return fechaIso;
    }
  }

  horaFormateada(fechaIso: string | null | undefined): string {
    if (!fechaIso) return '—';
    try {
      const d = new Date(fechaIso);
      if (isNaN(d.getTime())) {
        const match = fechaIso.match(/T?(\d{2}:\d{2})/);
        return match ? `${match[1]} h` : '—';
      }
      return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')} h`;
    } catch {
      return '—';
    }
  }

  duracionTexto(duracion: string | null | undefined): string {
    if (!duracion) return 'Por confirmar';
    return duracion;
  }

  caracteristicasTarifa(nombre: string): string[] {
    const n = (nombre || '').toLowerCase();
    if (n.includes('basic') || n.includes('básic') || n.includes('zero') || n.includes('promo')) {
      return [
        'Bolso de mano o mochila pequeña bajo el asiento',
        'Asiento asignado al azar',
        'Check-in digital disponible 48 h antes'
      ];
    }
    if (n.includes('light') || n.includes('classic') || n.includes('estándar') || n.includes('plus')) {
      return [
        'Bolso de mano + equipaje de mano de 10 kg',
        'Elección de asiento estándar incluida',
        'Cambios de fecha con cargo preferencial'
      ];
    }
    if (n.includes('premium') || n.includes('flex') || n.includes('top') || n.includes('ejecutiv')) {
      return [
        'Equipaje en bodega (23 kg) + equipaje de mano (10 kg)',
        'Elección de asiento preferente o con más espacio',
        'Cambios y devoluciones flexibles sin penalización',
        'Embarque prioritario'
      ];
    }
    return [
      'Equipaje de mano incluido',
      'Check-in online disponible',
      'Atención y asistencia durante el vuelo'
    ];
  }

  trackAeropuerto(_indice: number, aeropuerto: AeropuertoCatalogo): string {
    return aeropuerto.codigoIata;
  }

  intercambiar() {
    const { origen, destino } = this.form.getRawValue();
    this.form.patchValue({ origen: destino, destino: origen });
    this.itinerarioSeleccionado = null;
    if (this.resultados.length || this.busquedaRealizada) {
      this.resultados = [];


      this.busquedaRealizada = false;
    }
  }

  async buscar() {
    if (this.form.invalid || this.buscando) {
      this.form.markAllAsTouched();
      return;
    }

    const { origen, destino, fecha } = this.form.getRawValue();
    if (!origen || !destino) {
      this.form.markAllAsTouched();
      return;
    }
    if (origen.toUpperCase() === destino.toUpperCase()) {
      this.error = 'El aeropuerto de origen y destino deben ser distintos.';
      return;
    }

    this.buscando = true;
    this.error = '';
    this.busquedaRealizada = true;
    const fechaFiltro = fecha && fecha.trim().length > 0 ? fecha.trim() : null;
    this.itinerarioSeleccionado = null;
    this.resultados = [];



    try {
      this.resultados = await firstValueFrom(
        this.api.buscarItinerarios(origen.toUpperCase(), destino.toUpperCase(), fechaFiltro)
      );
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.buscando = false;
    }
  }

  async seleccionarItinerario(itinerario: ItinerarioDisponible): Promise<void> {
    this.itinerarioSeleccionado = itinerario;
    this.tarifaSeleccionada = null;
    this.vuelosItinerario = [];
    this.filasActivas = [];
    this.asientosSeleccionadosPorVuelo = {};
    this.vueloActivoIndex = 0;
    this.pasoActual = 2; // Avanza al Paso 2: Tarifa
    this.error = '';

    // Cargar vuelos y mapa de asientos en segundo plano
    void this.cargarVuelosAsientos(itinerario.idItinerario);
  }

  async cargarVuelosAsientos(idItinerario: number): Promise<void> {
    this.cargandoAsientos = true;
    try {
      this.vuelosItinerario = await firstValueFrom(this.api.vuelosAsientosItinerario(idItinerario));
      this.recalcularFilasVueloActivo();
    } catch (e) {
      console.warn('Error al cargar vuelos con asientos:', e);
      this.error = errorMessage(e);
    } finally {
      this.cargandoAsientos = false;
    }
  }

  deseleccionarItinerario(): void {
    this.itinerarioSeleccionado = null;
    this.tarifaSeleccionada = null;
    this.vuelosItinerario = [];
    this.filasActivas = [];
    this.asientosSeleccionadosPorVuelo = {};
    this.vueloActivoIndex = 0;
    this.pasoActual = 1;
  }

  precioMinimo(itinerario: ItinerarioDisponible): number | null {
    if (itinerario.tarifas && itinerario.tarifas.length > 0) {
      return Math.min(...itinerario.tarifas.map(t => Number(t.precio)));
    }
    return itinerario.precioBase ?? null;
  }

  seleccionarTarifa(tarifa: TarifaDisponible): void {
    this.tarifaSeleccionada = tarifa;
    this.error = '';
    this.asientosSeleccionadosPorVuelo = {};

    if (this.vuelosItinerario.length) {
      this.recalcularFilasVueloActivo();
    } else if (!this.cargandoAsientos && this.itinerarioSeleccionado) {
      void this.cargarVuelosAsientos(this.itinerarioSeleccionado.idItinerario);
    }

    this.pasoActual = 3; // Avanza al Paso 3: Asiento
  }

  // Navegación entre pasos
  irAPaso(paso: number): void {
    if (paso === 1) {
      this.deseleccionarItinerario();
    } else if (paso === 2 && this.itinerarioSeleccionado) {
      this.pasoActual = 2;
    } else if (paso === 3 && this.itinerarioSeleccionado && this.tarifaSeleccionada) {
      this.pasoActual = 3;
    } else if (paso === 4 && this.itinerarioSeleccionado && this.tarifaSeleccionada && this.todosLosAsientosSeleccionados()) {
      this.pasoActual = 4;
    } else if (paso === 5 && this.itinerarioSeleccionado && this.tarifaSeleccionada && this.todosLosAsientosSeleccionados()) {
      this.pasoActual = 5;
    }
  }

  volverAVuelos(): void {
    this.deseleccionarItinerario();
  }

  volverATarifas(): void {
    this.pasoActual = 2;
  }

  continuarAEquipaje(): void {
    if (!this.todosLosAsientosSeleccionados()) return;
    this.pasoActual = 4;
  }

  volverAAsiento(): void {
    this.pasoActual = 3;
  }

  continuarAlPago(): void {
    this.pasoActual = 5;
  }

  volverAEquipaje(): void {
    this.pasoActual = 4;
  }

  // Métodos de cabina y asientos
  get vueloActivo(): VueloConAsientos | null {
    if (!this.vuelosItinerario.length) return null;
    return this.vuelosItinerario[this.vueloActivoIndex] || this.vuelosItinerario[0];
  }

  cambiarVueloActivo(index: number): void {
    if (index >= 0 && index < this.vuelosItinerario.length) {
      this.vueloActivoIndex = index;
      this.recalcularFilasVueloActivo();
    }
  }

  asientoSeleccionadoEnVuelo(idVuelo: number): AsientoVueloDisponible | undefined {
    return this.asientosSeleccionadosPorVuelo[idVuelo];
  }

  seleccionarAsiento(vueloId: number, asiento: AsientoVueloDisponible): void {
    if (asiento.estado === 'ocupado') return;
    if (this.asientosSeleccionadosPorVuelo[vueloId]?.idAsiento === asiento.idAsiento) {
      delete this.asientosSeleccionadosPorVuelo[vueloId];
    } else {
      this.asientosSeleccionadosPorVuelo[vueloId] = asiento;
    }
  }

  isAsientoSeleccionado(vueloId: number, idAsiento: number): boolean {
    return this.asientosSeleccionadosPorVuelo[vueloId]?.idAsiento === idAsiento;
  }

  requiereSeleccionAsiento(v: VueloConAsientos): boolean {
    return Array.isArray(v.asientos) && v.asientos.some(a => a.estado === 'disponible');
  }

  todosLosAsientosSeleccionados(): boolean {
    if (this.cargandoAsientos) return false;
    if (!this.vuelosItinerario.length) return true;
    return this.vuelosItinerario.every(v => !this.requiereSeleccionAsiento(v) || !!this.asientosSeleccionadosPorVuelo[v.idVuelo]);
  }

  siguienteTramoSinAsientoIndex(): number {
    return this.vuelosItinerario.findIndex(v => this.requiereSeleccionAsiento(v) && !this.asientosSeleccionadosPorVuelo[v.idVuelo]);
  }

  irASiguienteTramoSinAsiento(): void {
    const idx = this.siguienteTramoSinAsientoIndex();
    if (idx >= 0) {
      this.cambiarVueloActivo(idx);
    }
  }

  trackFila(_index: number, fila: FilaAsientosView): number {
    return fila.numeroFila;
  }

  trackAsiento(_index: number, asiento: AsientoVueloDisponible): number {
    return asiento.idAsiento;
  }

  private recalcularFilasVueloActivo(): void {
    const v = this.vueloActivo;
    if (!v || !v.asientos.length) {
      this.filasActivas = [];
      return;
    }

    const mapFilas = new Map<number, AsientoVueloDisponible[]>();
    for (const a of v.asientos) {
      const f = a.fila || 1;
      if (!mapFilas.has(f)) mapFilas.set(f, []);
      mapFilas.get(f)!.push(a);
    }

    const resultado: FilaAsientosView[] = [];
    const filasOrdenadas = Array.from(mapFilas.keys()).sort((a, b) => a - b);
    let claseAnterior = '';

    for (const numFila of filasOrdenadas) {
      const asientosFila = [...mapFilas.get(numFila)!].sort((a, b) => (a.letra || '').localeCompare(b.letra || ''));
      const primer = asientosFila[0];
      const clase = primer?.clase || 'Económica';
      const precio = primer?.precio || 0;
      const esEmergencia = asientosFila.some(a => a.esEmergencia);
      const esPrimeraDeClase = clase.toLowerCase() !== claseAnterior.toLowerCase();
      claseAnterior = clase;

      const cfg = (v.configuracionesCabina || []).find(c => numFila >= c.filaInicio && numFila <= c.filaFin);
      let bloques: AsientoVueloDisponible[][] = [];

      if (cfg && cfg.distribucionColumnas) {
        const partes = cfg.distribucionColumnas.split('-').map(p => parseInt(p, 10)).filter(n => !isNaN(n) && n > 0);
        let cursor = 0;
        for (const tam of partes) {
          const bloque = asientosFila.slice(cursor, cursor + tam);
          if (bloque.length) bloques.push(bloque);
          cursor += tam;
        }
        if (cursor < asientosFila.length) {
          bloques.push(asientosFila.slice(cursor));
        }
      } else {
        if (asientosFila.length >= 6) {
          bloques = [asientosFila.slice(0, 3), asientosFila.slice(3)];
        } else if (asientosFila.length >= 4) {
          bloques = [asientosFila.slice(0, 2), asientosFila.slice(2)];
        } else {
          bloques = [asientosFila];
        }
      }

      resultado.push({
        numeroFila: numFila,
        clase,
        esPrimeraDeClase,
        esEmergencia,
        precio,
        bloques
      });
    }

    this.filasActivas = resultado;
  }

  // Métodos de equipaje
  puedeAgregarCabina(): boolean {
    return this.cantCabina < 1;
  }

  puedeAgregarBodega(): boolean {
    return (this.cantBodega15 + this.cantBodega23) < 3;
  }

  agregarCabina(): void {
    if (this.puedeAgregarCabina()) {
      this.cantCabina++;
    }
  }

  quitarCabina(): void {
    if (this.cantCabina > 0) {
      this.cantCabina--;
    }
  }

  agregarBodega15(): void {
    if (this.puedeAgregarBodega()) {
      this.cantBodega15++;
    }
  }

  quitarBodega15(): void {
    if (this.cantBodega15 > 0) {
      this.cantBodega15--;
    }
  }

  agregarBodega23(): void {
    if (this.puedeAgregarBodega()) {
      this.cantBodega23++;
    }
  }

  quitarBodega23(): void {
    if (this.cantBodega23 > 0) {
      this.cantBodega23--;
    }
  }

  // Totales
  totalAsientos(): number {
    let tot = 0;
    for (const v of this.vuelosItinerario) {
      const a = this.asientosSeleccionadosPorVuelo[v.idVuelo];
      if (a && a.precio) {
        tot += a.precio;
      }
    }
    return tot;
  }

  totalEquipaje(): number {
    return this.cantCabina * 15000 + this.cantBodega15 * 22000 + this.cantBodega23 * 30000;
  }

  totalPagar(): number {
    const precioBase = this.tarifaSeleccionada ? Number(this.tarifaSeleccionada.precio) : 0;
    return precioBase + this.totalAsientos() + this.totalEquipaje();
  }

  seleccionarMetodoPago(id: number): void {
    this.metodoPago = id;
  }

  async procesarPago(): Promise<void> {
    if (!this.itinerarioSeleccionado || !this.tarifaSeleccionada || this.procesandoPago) return;

    this.procesandoPago = true;
    this.error = '';

    const equipaje: ItemEquipajeSolicitud[] = [];
    if (this.cantCabina > 0) {
      equipaje.push({
        idTipoEquipaje: 1,
        tipo: 'CABINA',
        peso: 10,
        dimensiones: '40x30x20',
        precio: 15000
      });
    }
    for (let i = 0; i < this.cantBodega15; i++) {
      equipaje.push({
        idTipoEquipaje: 2,
        tipo: 'BODEGA_15KG',
        peso: 15,
        dimensiones: '158cm',
        precio: 22000
      });
    }
    for (let i = 0; i < this.cantBodega23; i++) {
      equipaje.push({
        idTipoEquipaje: 3,
        tipo: 'BODEGA_23KG',
        peso: 23,
        dimensiones: '158cm',
        precio: 30000
      });
    }

    const asientosSolicitud: AsientoSeleccionadoSolicitud[] = [];
    for (const v of this.vuelosItinerario) {
      const a = this.asientosSeleccionadosPorVuelo[v.idVuelo];
      if (a) {
        asientosSolicitud.push({ idVuelo: v.idVuelo, idAsiento: a.idAsiento });
      }
    }

    const reservaPayload = {
      idItinerario: this.itinerarioSeleccionado.idItinerario,
      idItinerarioTarifa: this.tarifaSeleccionada.idItinerarioTarifa,
      idMetodoPago: this.metodoPago,
      equipaje,
      asientos: asientosSolicitud
    };

    try {
      try {
        sessionStorage.setItem('skyway_mobile_checkout_pending', JSON.stringify({
          formValues: this.form.getRawValue(),
          resultados: this.resultados,
          itinerarioSeleccionado: this.itinerarioSeleccionado,
          tarifaSeleccionada: this.tarifaSeleccionada,
          vuelosItinerario: this.vuelosItinerario,
          asientosSeleccionadosPorVuelo: this.asientosSeleccionadosPorVuelo,
          cantCabina: this.cantCabina,
          cantBodega15: this.cantBodega15,
          cantBodega23: this.cantBodega23,
          metodoPago: this.metodoPago,
          reservaPayload
        }));
      } catch {
        // Ignorar si sessionStorage no está disponible
      }

      const urlBase = typeof window !== 'undefined' ? window.location.origin : 'http://localhost:8101';
      const inicioPago = await firstValueFrom(
        this.api.iniciarPagoReserva(
          this.itinerarioSeleccionado.idItinerario,
          this.tarifaSeleccionada.idItinerarioTarifa,
          this.metodoPago,
          equipaje,
          asientosSolicitud,
          urlBase
        )
      );

      if (inicioPago?.redirectUrl && typeof window !== 'undefined') {
        window.location.href = inicioPago.redirectUrl;
        return;
      }

      const res = await firstValueFrom(
        this.api.crearReserva(
          this.itinerarioSeleccionado.idItinerario,
          this.tarifaSeleccionada.idItinerarioTarifa,
          this.metodoPago,
          equipaje,
          asientosSolicitud
        )
      );
      this.reservaConfirmada = res;
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.procesandoPago = false;
    }
  }

  irADetalleReserva(idReserva: number): void {
    void this.router.navigate(['/vuelos/reservas', idReserva]);
  }

  irAMisVuelos(): void {
    void this.router.navigate(['/vuelos']);
  }

  nuevaBusqueda(): void {
    this.reservaConfirmada = null;
    this.tarifaSeleccionada = null;
    this.vuelosItinerario = [];
    this.filasActivas = [];
    this.asientosSeleccionadosPorVuelo = {};
    this.vueloActivoIndex = 0;
    this.pasoActual = 1;
    this.limpiarBusqueda();
  }
}
