import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Aeropuerto, DetalleReserva, Inicio, Itinerario, Pagina, ResumenReserva } from '../../core/models';
import { errorMessage } from '../../core/ui';
import { ParadaRuta, RutaVuelo3D, TramoRuta } from './globo.models';

const COORDENADAS_FALLBACK: Record<string, { lat: number; lon: number; ciudad: string; pais: string }> = {
  SCL: { lat: -33.3928, lon: -70.7858, ciudad: 'Santiago', pais: 'Chile' },
  LIM: { lat: -12.0219, lon: -77.1143, ciudad: 'Lima', pais: 'Perú' },
  EZE: { lat: -34.8222, lon: -58.5358, ciudad: 'Buenos Aires', pais: 'Argentina' },
  GRU: { lat: -23.4356, lon: -46.4731, ciudad: 'São Paulo', pais: 'Brasil' },
  BOG: { lat: 4.7016, lon: -74.1469, ciudad: 'Bogotá', pais: 'Colombia' },
  MIA: { lat: 25.7959, lon: -80.287, ciudad: 'Miami', pais: 'Estados Unidos' },
  JFK: { lat: 40.6413, lon: -73.7781, ciudad: 'Nueva York', pais: 'Estados Unidos' },
  MAD: { lat: 40.4839, lon: -3.5679, ciudad: 'Madrid', pais: 'España' },
  LAX: { lat: 33.9425, lon: -118.408, ciudad: 'Los Ángeles', pais: 'Estados Unidos' },
  PEK: { lat: 40.0799, lon: 116.603, ciudad: 'Beijing', pais: 'China' }
};

@Component({
  selector: 'app-vuelos',
  standalone: false,
  templateUrl: './vuelos.page.html',
  styleUrls: ['./vuelos.page.scss']
})
export class VuelosPage {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  seccion: 'proximo' | 'reservas' = 'proximo';
  inicio: Inicio | null = null;
  reservas: Pagina<ResumenReserva> | null = null;
  page = 0;
  busy = false;
  error = '';
  private generation = 0;

  // Modal del Globo 3D
  mostrarModalGlobo = false;
  cargandoRutaMapa = false;
  rutaSeleccionada: RutaVuelo3D | null = null;

  // Caché en memoria para evitar peticiones repetidas a la BD
  private readonly detalleReservaCache = new Map<number, DetalleReserva>();

  ionViewWillEnter() {
    void this.load();
  }

  cambiarSeccion(value: unknown) {
    if (value === 'proximo' || value === 'reservas') this.seccion = value;
  }

  irADetalleReserva(idReserva: number, event?: Event) {
    event?.stopPropagation();
    void this.router.navigate(['/vuelos/reservas', idReserva]);
  }

  abrirMapaDesdeCard(event: Event, r: ResumenReserva) {
    event.preventDefault();
    event.stopPropagation();
    void this.abrirMapaReserva(r.idReserva, null, r);
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

      // Pre-cargar en segundo plano los detalles en memoria mientras el token está vigente
      for (const r of reservas.contenido) {
        if (!this.detalleReservaCache.has(r.idReserva)) {
          this.api.reserva(r.idReserva).subscribe({
            next: d => this.detalleReservaCache.set(r.idReserva, d),
            error: () => {}
          });
        }
      }
      if (inicio.proximoVuelo && !this.detalleReservaCache.has(inicio.proximoVuelo.idReserva)) {
        this.api.reserva(inicio.proximoVuelo.idReserva).subscribe({
          next: d => this.detalleReservaCache.set(inicio.proximoVuelo!.idReserva, d),
          error: () => {}
        });
      }
    } catch (e) {
      if (generation === this.generation) this.error = errorMessage(e);
    } finally {
      if (generation === this.generation) this.busy = false;
    }
  }

  async abrirMapaReserva(idReserva: number, idVuelo?: number | null, resumenFallback?: ResumenReserva) {
    this.cargandoRutaMapa = true;
    try {
      let detalle = this.detalleReservaCache.get(idReserva);
      if (!detalle) {
        detalle = await firstValueFrom(this.api.reserva(idReserva));
        this.detalleReservaCache.set(idReserva, detalle);
      }

      this.rutaSeleccionada = this.procesarRutaVuelo(detalle, idVuelo);
      this.mostrarModalGlobo = true;
    } catch (e) {
      console.warn('Error al obtener ruta de reserva, recurriendo a fallback resiliente:', e);
      this.rutaSeleccionada = this.construirRutaFallback(idReserva, idVuelo, resumenFallback);
      this.mostrarModalGlobo = true;
    } finally {
      this.cargandoRutaMapa = false;
    }
  }

  cerrarModalGlobo() {
    this.mostrarModalGlobo = false;
    this.rutaSeleccionada = null;
  }

  private procesarRutaVuelo(detalle: DetalleReserva, idVueloTarget?: number | null): RutaVuelo3D {
    // 1. Identificar el itinerario correspondiente
    let it: Itinerario | undefined;
    if (idVueloTarget && detalle.itinerarios?.length) {
      it = detalle.itinerarios.find(i =>
        i.vuelos?.some(cv => cv.vuelo?.idVuelo === idVueloTarget)
      );
    }
    if (!it && detalle.itinerarios?.length) {
      it = detalle.itinerarios[0];
    }

    const paradasMap = new Map<string, ParadaRuta>();
    const paradasList: ParadaRuta[] = [];

    const registrarParada = (
      apt: Aeropuerto | null | undefined,
      tipo: 'origen' | 'escala' | 'destino',
      numVuelo?: string | null,
      espera?: string | null,
      tipoCon?: string | null
    ) => {
      if (!apt) return;
      const iata = (apt.codigoIata || '').trim().toUpperCase();
      if (!iata) return;

      const fallback = COORDENADAS_FALLBACK[iata];
      const lat = apt.latitud ?? fallback?.lat ?? 0;
      const lon = apt.longitud ?? fallback?.lon ?? 0;
      const ciudad = apt.ciudad || fallback?.ciudad || 'Ciudad';
      const pais = apt.pais || fallback?.pais || 'País';
      const nombre = apt.nombreAeropuerto || iata;

      const parada: ParadaRuta = {
        codigoIata: iata,
        nombreAeropuerto: nombre,
        ciudad,
        pais,
        latitud: lat,
        longitud: lon,
        tipo,
        colorHex: tipo === 'origen' ? '#ff4d4d' : tipo === 'destino' ? '#3ddc84' : '#ffd440',
        numeroVuelo: numVuelo,
        tiempoEspera: espera,
        tipoConexion: tipoCon
      };

      // Si no existe, agregar a la lista
      if (!paradasMap.has(iata)) {
        paradasMap.set(iata, parada);
        paradasList.push(parada);
      } else {
        // Actualizar datos si es relevante
        const existing = paradasMap.get(iata)!;
        if (espera) existing.tiempoEspera = espera;
        if (numVuelo) existing.numeroVuelo = numVuelo;
      }
    };

    let horaSalidaPrimerTramo: string | null = null;
    let horaLlegadaUltimoTramo: string | null = null;

    if (it) {
      horaSalidaPrimerTramo = it.horaSalida ? String(it.horaSalida) : null;
      horaLlegadaUltimoTramo = it.horaLlegada ? String(it.horaLlegada) : null;

      const conexiones = (it.vuelos || []).slice().sort((a: any, b: any) => a.orden - b.orden);

      if (conexiones.length > 0) {
        for (const conn of conexiones) {
          const v = conn.vuelo;
          if (!horaSalidaPrimerTramo && v.fechaHoraSalida) {
            horaSalidaPrimerTramo = String(v.fechaHoraSalida);
          }
          if (v.fechaHoraLlegada) {
            horaLlegadaUltimoTramo = String(v.fechaHoraLlegada);
          }

          const segmentos = (v.segmentos || []).slice().sort((a: any, b: any) => a.ordenSegmento - b.ordenSegmento);
          if (segmentos.length > 0) {
            for (let sIdx = 0; sIdx < segmentos.length; sIdx++) {
              const seg = segmentos[sIdx];
              if (sIdx === 0 && paradasList.length === 0) {
                registrarParada(seg.origen, 'origen', v.numeroVuelo);
              }
              const esUltimo = sIdx === segmentos.length - 1 && conn === conexiones[conexiones.length - 1];
              registrarParada(
                seg.destino,
                esUltimo ? 'destino' : 'escala',
                v.numeroVuelo,
                conn.tiempoEspera,
                conn.tipoConexion
              );
            }
          } else {
            // Vuelo sin segmentos detallados, usar aeropuertos del itinerario
            if (paradasList.length === 0 && it.origen) {
              registrarParada(it.origen, 'origen', v.numeroVuelo);
            }
            if (it.destino) {
              registrarParada(it.destino, 'destino', v.numeroVuelo, conn.tiempoEspera, conn.tipoConexion);
            }
          }
        }
      } else if (it.origen && it.destino) {
        registrarParada(it.origen, 'origen');
        registrarParada(it.destino, 'destino');
      }
    }

    // Asegurar que la primera parada sea origen y la última sea destino
    if (paradasList.length > 0) {
      paradasList[0].tipo = 'origen';
      paradasList[0].colorHex = '#ff4d4d';
    }
    if (paradasList.length > 1) {
      paradasList[paradasList.length - 1].tipo = 'destino';
      paradasList[paradasList.length - 1].colorHex = '#3ddc84';
      for (let i = 1; i < paradasList.length - 1; i++) {
        paradasList[i].tipo = 'escala';
        paradasList[i].colorHex = '#ffd440';
      }
    }

    // 2. Construir tramos y calcular distancia real con Haversine
    const tramos: TramoRuta[] = [];
    let distanciaTotalKm = 0;

    for (let i = 0; i < paradasList.length - 1; i++) {
      const orig = paradasList[i];
      const dest = paradasList[i + 1];
      const dKm = this.calcularHaversineKm(orig.latitud, orig.longitud, dest.latitud, dest.longitud);
      distanciaTotalKm += dKm;
      tramos.push({
        origen: orig,
        destino: dest,
        numeroVuelo: dest.numeroVuelo,
        distanciaKm: dKm
      });
    }

    // 3. Duración total
    const duracionTexto = this.calcularDuracionTexto(horaSalidaPrimerTramo, horaLlegadaUltimoTramo);

    // 4. Escalas
    const numEscalas = Math.max(0, paradasList.length - 2);
    const nombresEscalas = paradasList.slice(1, -1).map(p => p.codigoIata);

    const origenFinal = paradasList[0];
    const destinoFinal = paradasList[paradasList.length - 1] || origenFinal;

    return {
      idReserva: detalle.idReserva,
      codigoOrigen: origenFinal ? origenFinal.codigoIata : '—',
      codigoDestino: destinoFinal ? destinoFinal.codigoIata : '—',
      ciudadOrigen: origenFinal ? origenFinal.ciudad : '—',
      ciudadDestino: destinoFinal ? destinoFinal.ciudad : '—',
      paisOrigen: origenFinal ? origenFinal.pais : '—',
      paisDestino: destinoFinal ? destinoFinal.pais : '—',
      paradas: paradasList,
      tramos,
      distanciaKm: distanciaTotalKm,
      duracionTotal: duracionTexto,
      numEscalas,
      nombresEscalas
    };
  }

  private calcularHaversineKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return Math.round(R * c);
  }

  private calcularDuracionTexto(inicioIso?: string | null, finIso?: string | null): string {
    if (!inicioIso || !finIso) return '~3h';
    const t1 = new Date(inicioIso).getTime();
    const t2 = new Date(finIso).getTime();
    if (isNaN(t1) || isNaN(t2) || t2 <= t1) return '~3h';
    const diffMin = Math.round((t2 - t1) / (1000 * 60));
    const horas = Math.floor(diffMin / 60);
    const minutos = diffMin % 60;
    if (horas === 0) return `~${minutos}m`;
    if (minutos === 0) return `~${horas}h`;
    return `~${horas}h ${minutos}m`;
  }

  private construirRutaFallback(
    idReserva: number,
    idVuelo?: number | null,
    resumenFallback?: ResumenReserva
  ): RutaVuelo3D {
    if (this.inicio?.proximoVuelo && this.inicio.proximoVuelo.idReserva === idReserva) {
      const pv = this.inicio.proximoVuelo;
      const dummyDetalle: DetalleReserva = {
        idReserva,
        fechaReserva: '',
        estadoReserva: { idEstadoReserva: null, descripcion: null },
        total: null,
        titular: true,
        checkin: null,
        equipajes: [],
        itinerarios: [{
          idReservaItinerario: 0,
          idItinerario: 0,
          horaSalida: pv.vuelo.fechaHoraSalida,
          horaLlegada: pv.vuelo.fechaHoraLlegada,
          numeroEscalas: pv.vuelo.segmentos?.length ? Math.max(0, pv.vuelo.segmentos.length - 1) : 0,
          precioBase: null,
          origen: pv.vuelo.segmentos?.[0]?.origen ?? null,
          destino: pv.vuelo.segmentos?.[pv.vuelo.segmentos.length - 1]?.destino ?? null,
          tarifa: { idItinerarioTarifa: 0, idTarifa: 0, nombre: '', precio: 0, caracteristicas: [] },
          vuelos: [{
            idItinerarioVuelo: 0,
            orden: 1,
            tiempoEspera: null,
            tipoConexion: null,
            vuelo: pv.vuelo
          }]
        }]
      };
      return this.procesarRutaVuelo(dummyDetalle, idVuelo);
    }

    const res = resumenFallback || this.reservas?.contenido.find(r => r.idReserva === idReserva);
    const origIata = (res?.codigoIataOrigen || 'SCL').trim().toUpperCase();
    const destIata = (res?.codigoIataDestino || 'LIM').trim().toUpperCase();

    const origCoord = COORDENADAS_FALLBACK[origIata] || { lat: -33.3928, lon: -70.7858, ciudad: origIata, pais: 'Chile' };
    const destCoord = COORDENADAS_FALLBACK[destIata] || { lat: -12.0219, lon: -77.1143, ciudad: destIata, pais: 'Perú' };

    const origParada: ParadaRuta = {
      codigoIata: origIata,
      nombreAeropuerto: `Aeropuerto ${origCoord.ciudad}`,
      ciudad: origCoord.ciudad,
      pais: origCoord.pais,
      latitud: origCoord.lat,
      longitud: origCoord.lon,
      tipo: 'origen',
      colorHex: '#ff4d4d'
    };

    const destParada: ParadaRuta = {
      codigoIata: destIata,
      nombreAeropuerto: `Aeropuerto ${destCoord.ciudad}`,
      ciudad: destCoord.ciudad,
      pais: destCoord.pais,
      latitud: destCoord.lat,
      longitud: destCoord.lon,
      tipo: 'destino',
      colorHex: '#3ddc84'
    };

    const dKm = this.calcularHaversineKm(origParada.latitud, origParada.longitud, destParada.latitud, destParada.longitud);
    const tramo: TramoRuta = {
      origen: origParada,
      destino: destParada,
      distanciaKm: dKm
    };

    return {
      idReserva,
      codigoOrigen: origIata,
      codigoDestino: destIata,
      ciudadOrigen: origCoord.ciudad,
      ciudadDestino: destCoord.ciudad,
      paisOrigen: origCoord.pais,
      paisDestino: destCoord.pais,
      paradas: [origParada, destParada],
      tramos: [tramo],
      distanciaKm: dKm,
      duracionTotal: '~3h',
      numEscalas: 0,
      nombresEscalas: []
    };
  }
}
