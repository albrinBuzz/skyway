import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Capacitor } from '@capacitor/core';
import { firstValueFrom } from 'rxjs';
import { addIcons } from 'ionicons';
import {
  airplane,
  airplaneOutline,
  alertCircle,
  alertCircleOutline,
  arrowForwardOutline,
  briefcaseOutline,
  calendarOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  chevronForwardOutline,
  closeCircleOutline,
  closeOutline,
  informationCircle,
  informationCircleOutline,
  locationOutline,
  pricetagOutline,
  refreshOutline,
  search,
  searchOutline,
  shieldCheckmarkOutline,
  sparklesOutline,
  swapHorizontalOutline,
  time,
  timeOutline,
  trashOutline
} from 'ionicons/icons';
import { ApiService } from '../../core/api.service';
import { AeropuertoCatalogo, ItinerarioDisponible, TarifaDisponible } from '../../core/models';
import { errorMessage } from '../../core/ui';

addIcons({
  airplane,
  airplaneOutline,
  alertCircle,
  alertCircleOutline,
  arrowForwardOutline,
  briefcaseOutline,
  calendarOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  chevronForwardOutline,
  closeCircleOutline,
  closeOutline,
  informationCircle,
  informationCircleOutline,
  locationOutline,
  pricetagOutline,
  refreshOutline,
  search,
  searchOutline,
  shieldCheckmarkOutline,
  sparklesOutline,
  swapHorizontalOutline,
  time,
  timeOutline,
  trashOutline
});

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

  aeropuertos: AeropuertoCatalogo[] = [];
  aeropuertosFiltrados: AeropuertoCatalogo[] = [];
  selectorAbierto = false;
  campoSelector: 'origen' | 'destino' | null = null;
  codigoSeleccionado = '';
  textoAeropuerto = '';
  resultados: ItinerarioDisponible[] = [];
  cargandoCatalogo = false;
  buscando = false;
  busquedaRealizada = false;
  error = '';

  readonly hoy = this.fechaLocal(new Date());

  readonly form = this.fb.nonNullable.group({
    origen: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
    destino: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
    fecha: [this.hoy, Validators.required]
  });

  get tieneSeleccionOBusqueda(): boolean {
    return Boolean(
      this.form.controls.origen.value ||
      this.form.controls.destino.value ||
      this.resultados.length ||
      this.busquedaRealizada ||
      this.error
    );
  }

  limpiarBusqueda(): void {
    this.form.patchValue({ origen: '', destino: '' });
    this.form.markAsPristine();
    this.form.markAsUntouched();
    this.form.controls.origen.markAsPristine();
    this.form.controls.origen.markAsUntouched();
    this.form.controls.destino.markAsPristine();
    this.form.controls.destino.markAsUntouched();
    this.resultados = [];
    this.busquedaRealizada = false;
    this.error = '';
    this.codigoSeleccionado = '';
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
    }
    this.resultados = [];
    this.busquedaRealizada = false;
    this.error = '';
  }

  quitarSeleccionModal(): void {
    if (this.campoSelector) {
      this.limpiarCampo(this.campoSelector);
    }
    this.codigoSeleccionado = '';
    this.cerrarSelector();
  }

  ionViewWillEnter() {
    if (!this.aeropuertos.length && !this.cargandoCatalogo) void this.cargarAeropuertos();
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
    if (origen.toUpperCase() === destino.toUpperCase()) {
      this.error = 'El aeropuerto de origen y destino deben ser distintos.';
      return;
    }

    this.buscando = true;
    this.error = '';
    this.busquedaRealizada = true;
    this.resultados = [];

    try {
      this.resultados = await firstValueFrom(
        this.api.buscarItinerarios(origen.toUpperCase(), destino.toUpperCase(), fecha)
      );
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.buscando = false;
    }
  }

  /**
   * Enlaza al flujo WEB que SkyWay ya utiliza para elegir asientos, equipaje y pagar.
   * IMPORTANTE: La web espera idTarifa, no idItinerarioTarifa.
   * No crea reservas ni registra pagos desde la aplicación Ionic.
   */
  urlCompraWeb(itinerario: ItinerarioDisponible, tarifa: TarifaDisponible): string {
    const url = new URL('/home/seleccionAsientos.xhtml', this.api.getWebBaseUrl());
    url.searchParams.set('itinerarios', String(itinerario.idItinerario));
    url.searchParams.set('tarifas', String(tarifa.idTarifa));
    url.searchParams.set('adultos', '1');
    return url.toString();
  }

  /**
   * Ruta alternativa: la página web muestra los vuelos filtrados por la misma
   * búsqueda y permite escoger allí la tarifa y completar la reserva.
   */
  urlResultadosWeb(itinerario: ItinerarioDisponible): string {
    const url = new URL('/home/vuelos.xhtml', this.api.getWebBaseUrl());
    const { fecha } = this.form.getRawValue();
    url.searchParams.set('salida', itinerario.origen);
    url.searchParams.set('llegada', itinerario.destino);
    url.searchParams.set('fechaIda', fecha);
    url.searchParams.set('adultos', '1');
    url.searchParams.set('trip', 'OW');
    return url.toString();
  }
}
