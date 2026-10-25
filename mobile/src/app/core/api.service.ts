import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Capacitor } from '@capacitor/core';
import { Observable, of } from 'rxjs';
import { catchError, map, timeout } from 'rxjs/operators';
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

export const AZURE_CLOUD_HOST = 'https://skyway-gde6a6aza3abb8a6.brazilsouth-01.azurewebsites.net';
export const DEFAULT_MIPC_HOST = 'http://10.227.14.199:8080';
export const HOME_MIPC_HOST = 'http://192.168.100.7:8080';
export const DEFAULT_NATIVE_HOST = AZURE_CLOUD_HOST;

const SERVER_HOST_KEY = 'skyway_server_host';
const SERVER_MODE_KEY = 'skyway_server_mode';
const SERVER_HOST_MIPC_KEY = 'skyway_host_mipc';
const SERVER_HOST_OTRO_KEY = 'skyway_host_otro';

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

  public normalizeOrigin(raw: string): string | null {
    if (!raw || typeof raw !== 'string') return null;
    const cleaned = raw.trim();
    if (!cleaned) return null;

    // Rechazar esquemas de URL peligrosos o no soportados
    if (/^(?:javascript|data|file|vbscript|blob|about):/i.test(cleaned)) {
      return null;
    }

    // Si incluye cualquier esquema con '://', debe ser exclusivamente http o https
    if (/^[a-zA-Z][a-zA-Z0-9+.-]*:\/\//.test(cleaned) && !/^https?:\/\//i.test(cleaned)) {
      return null;
    }

    // Quitar trailing slashes y sufijo de API móvil
    const trimmed = cleaned.replace(/\/+$/, '').replace(/\/api\/v1\/mobile$/i, '');
    if (!trimmed || /^https?:\/\/?$/i.test(trimmed) || /^https?:$/i.test(trimmed)) {
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
      // Rechazar hostnames con espacios o terminaciones inválidas
      if (parsed.hostname.includes(' ') || parsed.hostname.startsWith('.') || parsed.hostname.endsWith('.')) {
        return null;
      }
      if (!parsed.port && (/^\d+\.\d+\.\d+\.\d+$/.test(parsed.hostname) || parsed.hostname === 'localhost')) {
        parsed.port = '8080';
      }
      return parsed.origin;
    } catch {
      return null;
    }
  }

  hasExplicitServerMode(): boolean {
    try {
      const saved = localStorage.getItem(SERVER_MODE_KEY);
      return saved === 'azure' || saved === 'mipc' || saved === 'otro' || Boolean(this.customOrigin);
    } catch {
      return Boolean(this.customOrigin);
    }
  }

  getServerMode(): 'azure' | 'mipc' | 'otro' {
    try {
      const saved = localStorage.getItem(SERVER_MODE_KEY);
      if (saved === 'azure' || saved === 'mipc' || saved === 'otro') {
        return saved;
      }
    } catch {
      // Ignorar fallo de almacenamiento
    }

    const current = (this.customOrigin || '').toLowerCase();
    if (current.includes('azurewebsites.net')) return 'azure';
    if (
      current.includes('192.168.') ||
      current.includes('10.') ||
      current.includes('localhost') ||
      current.includes('127.0.0.1')
    ) {
      return 'mipc';
    }
    if (current) {
      return 'otro';
    }
    return Capacitor.isNativePlatform() ? 'azure' : 'mipc';
  }

  setServerMode(mode: 'azure' | 'mipc' | 'otro'): void {
    try {
      localStorage.setItem(SERVER_MODE_KEY, mode);
    } catch {
      // Ignorar fallo de almacenamiento
    }
  }

  getSavedHostForMode(mode: 'azure' | 'mipc' | 'otro'): string {
    if (mode === 'azure') return AZURE_CLOUD_HOST;
    try {
      if (mode === 'mipc') {
        return localStorage.getItem(SERVER_HOST_MIPC_KEY) || DEFAULT_MIPC_HOST;
      }
      if (mode === 'otro') {
        return localStorage.getItem(SERVER_HOST_OTRO_KEY) || '';
      }
    } catch {
      // Fallback
    }
    return mode === 'mipc' ? DEFAULT_MIPC_HOST : '';
  }

  setSavedHostForMode(mode: 'azure' | 'mipc' | 'otro', rawHost: string): void {
    try {
      const normalized = this.normalizeOrigin(rawHost) || (rawHost ?? '').trim();
      if (mode === 'mipc') {
        localStorage.setItem(SERVER_HOST_MIPC_KEY, normalized);
      } else if (mode === 'otro') {
        localStorage.setItem(SERVER_HOST_OTRO_KEY, normalized);
      }
    } catch {
      // Ignorar fallo de almacenamiento
    }
  }

  getServerHost(): string {
    if (this.customOrigin) return this.customOrigin;

    let savedMode: string | null = null;
    try {
      savedMode = localStorage.getItem(SERVER_MODE_KEY);
    } catch {
      savedMode = null;
    }

    if (savedMode === 'otro') {
      const savedOtro = this.getSavedHostForMode('otro');
      const normalized = this.normalizeOrigin(savedOtro);
      if (normalized) {
        this.customOrigin = normalized;
        return normalized;
      }
      return '';
    }
    if (savedMode === 'mipc') {
      const savedMipc = this.getSavedHostForMode('mipc') || DEFAULT_MIPC_HOST;
      return this.normalizeOrigin(savedMipc) || DEFAULT_MIPC_HOST;
    }
    if (savedMode === 'azure') {
      return AZURE_CLOUD_HOST;
    }

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

  probarConexion(rawHost?: string): Observable<{ ok: boolean; host: string; mensaje: string }> {
    const target = rawHost ? this.normalizeOrigin(rawHost) : this.getServerHost();
    if (!target) {
      return of({ ok: false, host: '', mensaje: 'URL inválida o no configurada' });
    }
    const testUrl = `${target}/api/v1/mobile/auth/login`;
    return this.http.post(testUrl, {}, { observe: 'response' }).pipe(
      timeout(4500),
      map(() => ({
        ok: true,
        host: target,
        mensaje: 'Conexión exitosa con el servidor SkyWay'
      })),
      catchError((err) => {
        if (err && typeof err.status === 'number' && err.status > 0) {
          // Si el servidor HTTP respondió (400, 401, 403, 405), el backend está vivo y alcanzable
          return of({
            ok: true,
            host: target,
            mensaje: `Conexión confirmada (HTTP ${err.status})`
          });
        }
        const isTimeout = err?.name === 'TimeoutError';
        return of({
          ok: false,
          host: target,
          mensaje: isTimeout
            ? 'Tiempo de espera agotado (4.5s). Servidor no responde.'
            : 'No se pudo conectar al servidor. Verifica la IP o red Wi-Fi.'
        });
      })
    );
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
