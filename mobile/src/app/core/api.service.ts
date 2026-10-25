import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Capacitor } from '@capacitor/core';
import { environment } from '../../environments/environment';
import {
  AeropuertoCatalogo,
  Checkin,
  DetalleReserva,
  Equipaje,
  Inicio,
  ItinerarioDisponible,
  LoginResponse,
  MapaAsientos,
  Notificacion,
  Pagina,
  PaseAbordar,
  Perfil,
  ReservaCreada,
  ResumenReserva,
  Vuelo,
  PasajeroTicket,
  VistaTicket
} from './models';

const SERVER_HOST_KEY = 'skyway_server_host';
export const DEFAULT_NATIVE_HOST = 'http://192.168.100.7:8080';
export const AZURE_CLOUD_HOST = 'https://skyway-gde6a6aza3abb8a6.brazilsouth-01.azurewebsites.net';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private customOrigin: string | null = this.loadSavedOrigin();

  private loadSavedOrigin(): string | null {
    try {
      const stored = localStorage.getItem(SERVER_HOST_KEY);
      return stored ? this.normalizeOrigin(stored) : null;
    } catch {
      return null;
    }
  }

  private normalizeOrigin(raw: string): string | null {
    if (!raw || typeof raw !== 'string') return null;
    const trimmed = raw.trim().replace(/\/+$/, '').replace(/\/api\/v1\/mobile$/i, '');
    if (!trimmed) return null;

    // Rechazar esquemas de URL peligrosos de forma inmediata
    if (/^(?:javascript|data|file|vbscript|blob):/i.test(trimmed)) {
      return null;
    }

    const withProtocol = /^https?:\/\//i.test(trimmed) ? trimmed : `http://${trimmed}`;
    try {
      const parsed = new URL(withProtocol);
      // Validar estrictamente protocolos http: o https: (rechaza javascript:, data:, etc.)
      if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
        return null;
      }
      // Validar hostname válido sin credenciales ni inyecciones
      if (!parsed.hostname || parsed.username || parsed.password) {
        return null;
      }
      if (!parsed.port && /^\d+\.\d+\.\d+\.\d+$/.test(parsed.hostname)) {
        parsed.port = '8080';
      }
      return parsed.origin;
    } catch {
      return null;
    }
  }

  getServerHost(): string {
    if (this.customOrigin) return this.customOrigin;
    if (Capacitor.isNativePlatform()) return DEFAULT_NATIVE_HOST;
    const envBase = environment.apiUrl.replace(/\/$/, '');
    if (/^https?:\/\//i.test(envBase)) {
      return envBase.replace(/\/api\/v1\/mobile$/i, '');
    }
    return '';
  }

  setServerHost(rawHost: string): void {
    const normalized = this.normalizeOrigin(rawHost);
    this.customOrigin = normalized;
    try {
      if (normalized) {
        localStorage.setItem(SERVER_HOST_KEY, normalized);
      } else {
        localStorage.removeItem(SERVER_HOST_KEY);
      }
    } catch {
      // Ignorar si el almacenamiento local no está disponible
    }
  }

  getWebBaseUrl(): string {
    return this.getServerHost() || 'http://localhost:8080';
  }

  get apiBaseUrl(): string {
    return this.base;
  }

  private get base(): string {
    const host = this.getServerHost();
    if (host) return `${host}/api/v1/mobile`;
    return environment.apiUrl.replace(/\/$/, '');
  }

  login(correoElectronico: string, contrasena: string) {
    return this.http.post<LoginResponse>(`${this.base}/auth/login`, { correoElectronico, contrasena });
  }

  me() {
    return this.http.get<Perfil>(`${this.base}/auth/me`);
  }

  actualizarCorreo(correoElectronico: string, contrasenaActual: string) {
    return this.http.patch<Perfil>(`${this.base}/auth/perfil/correo`, { correoElectronico, contrasenaActual });
  }

  cambiarContrasena(contrasenaActual: string, nuevaContrasena: string) {
    return this.http.patch<void>(`${this.base}/auth/perfil/contrasena`, { contrasenaActual, nuevaContrasena });
  }

  inicio() {
    return this.http.get<Inicio>(`${this.base}/inicio`);
  }

  aeropuertos() {
    return this.http.get<AeropuertoCatalogo[]>(`${this.base}/catalogo/aeropuertos`);
  }

  buscarItinerarios(origen: string, destino: string, fecha: string) {
    return this.http.get<ItinerarioDisponible[]>(`${this.base}/busqueda/itinerarios`, {
      params: { origen, destino, fecha }
    });
  }

  crearReserva(idItinerario: number, idItinerarioTarifa: number) {
    return this.http.post<ReservaCreada>(`${this.base}/reservas`, { idItinerario, idItinerarioTarifa });
  }

  reservas(grupo: 'activas' | 'historial', pagina = 0, tamano = 20) {
    return this.http.get<Pagina<ResumenReserva>>(`${this.base}/reservas`, {
      params: { grupo, pagina, tamano }
    });
  }

  reserva(id: number) {
    return this.http.get<DetalleReserva>(`${this.base}/reservas/${id}`);
  }

  vuelo(id: number, vuelo: number) {
    return this.http.get<Vuelo>(`${this.base}/reservas/${id}/vuelos/${vuelo}`);
  }

  asientos(id: number, vuelo: number) {
    return this.http.get<MapaAsientos>(`${this.base}/reservas/${id}/vuelos/${vuelo}/asientos`);
  }

  cambiarAsiento(id: number, vuelo: number, idAsiento: number) {
    return this.http.put<MapaAsientos>(`${this.base}/reservas/${id}/vuelos/${vuelo}/asiento`, { idAsiento });
  }

  equipajes(id: number) {
    return this.http.get<Equipaje[]>(`${this.base}/reservas/${id}/equipajes`);
  }

  checkin(id: number) {
    return this.http.post<Checkin>(`${this.base}/reservas/${id}/checkin`, {});
  }

  pase(id: number, vuelo: number) {
    return this.http.get<PaseAbordar>(`${this.base}/reservas/${id}/vuelos/${vuelo}/pase-abordar`);
  }

  /** Personas efectivamente vinculadas a la reserva y al vuelo. */
  ticketsReserva(idReserva: number, idVuelo: number) {
    return this.http.get<PasajeroTicket[]>(`${this.base}/reservas/${idReserva}/vuelos/${idVuelo}/tickets`);
  }

  /** Ticket particular. El RUT es el identificador de un pasajero de ESTA reserva. */
  vistaTicket(idReserva: number, idVuelo: number, rut: string) {
    return this.http.get<VistaTicket>(`${this.base}/reservas/${idReserva}/vuelos/${idVuelo}/tickets/${encodeURIComponent(rut)}/vista`);
  }

  /** Mapa y cambio de asiento de un acompañante autorizados exclusivamente en el backend. */
  asientosPasajero(idReserva: number, idVuelo: number, rut: string) {
    return this.http.get<MapaAsientos>(`${this.base}/reservas/${idReserva}/vuelos/${idVuelo}/tickets/${encodeURIComponent(rut)}/asientos`);
  }

  cambiarAsientoPasajero(idReserva: number, idVuelo: number, rut: string, idAsiento: number) {
    return this.http.put<MapaAsientos>(`${this.base}/reservas/${idReserva}/vuelos/${idVuelo}/tickets/${encodeURIComponent(rut)}/asiento`, { idAsiento });
  }

  notificaciones(pagina = 0) {
    return this.http.get<Pagina<Notificacion>>(`${this.base}/notificaciones`, {
      params: { pagina, tamano: 20 }
    });
  }

  leerNotificacion(id: number) {
    return this.http.patch<Notificacion>(`${this.base}/notificaciones/${id}/leida`, {});
  }
}
