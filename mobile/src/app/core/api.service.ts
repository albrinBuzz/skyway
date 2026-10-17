import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl.replace(/\/$/, '');

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
