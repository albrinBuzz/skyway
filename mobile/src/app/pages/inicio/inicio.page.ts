import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Capacitor } from '@capacitor/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AeropuertoCatalogo, ItinerarioDisponible, TarifaDisponible } from '../../core/models';
import { errorMessage } from '../../core/ui';

/**
 * Dirección HTTP(S) pública de la web SkyWay (Spring Boot/JSF).
 * Desarrollo local: http://localhost:8080
 * Para celulares reales o producción, cambiar por URL accesible desde el dispositivo.
 */
const URL_WEB_SKYWAY = Capacitor.isNativePlatform() ? 'http://192.168.100.7:8080' : 'http://localhost:8080';

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
          `${a.codigoIata} ${a.ciudad ?? ''} ${a.nombreAeropuerto}`
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
    if (!codigo) return 'Busca aeropuerto o IATA';
    const aeropuerto = this.aeropuertos.find(a => a.codigoIata === codigo);
    return aeropuerto ? `${aeropuerto.codigoIata} · ${aeropuerto.ciudad || aeropuerto.nombreAeropuerto}` : codigo;
  }

  nombreSeleccionado(codigo: string): string {
    return this.aeropuertos.find(a => a.codigoIata === codigo)?.nombreAeropuerto ?? '';
  }

  trackAeropuerto(_indice: number, aeropuerto: AeropuertoCatalogo): string {
    return aeropuerto.codigoIata;
  }

  intercambiar() {
    const { origen, destino } = this.form.getRawValue();
    this.form.patchValue({ origen: destino, destino: origen });
  }

  async buscar() {
    if (this.form.invalid || this.buscando) {
      this.form.markAllAsTouched();
      return;
    }

    const { origen, destino, fecha } = this.form.getRawValue();
    if (origen === destino) {
      this.error = 'El origen y el destino deben ser distintos.';
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
