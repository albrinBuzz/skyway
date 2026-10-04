import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { LoginResponse, Perfil, Inicio, Pagina, ResumenReserva, DetalleReserva, Vuelo, MapaAsientos, Checkin, PaseAbordar, Notificacion, Equipaje } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl.replace(/\/$/, '');
  login(correoElectronico: string, contrasena: string) { return this.http.post<LoginResponse>(`${this.base}/auth/login`, { correoElectronico, contrasena }); }
  me() { return this.http.get<Perfil>(`${this.base}/auth/me`); }
  inicio() { return this.http.get<Inicio>(`${this.base}/inicio`); }
  reservas(grupo: 'activas' | 'historial', pagina = 0, tamano = 20) { return this.http.get<Pagina<ResumenReserva>>(`${this.base}/reservas`, { params: { grupo, pagina, tamano } }); }
  reserva(id: number) { return this.http.get<DetalleReserva>(`${this.base}/reservas/${id}`); }
  vuelo(id: number, vuelo: number) { return this.http.get<Vuelo>(`${this.base}/reservas/${id}/vuelos/${vuelo}`); }
  asientos(id: number, vuelo: number) { return this.http.get<MapaAsientos>(`${this.base}/reservas/${id}/vuelos/${vuelo}/asientos`); }
  cambiarAsiento(id: number, vuelo: number, idAsiento: number) { return this.http.put<MapaAsientos>(`${this.base}/reservas/${id}/vuelos/${vuelo}/asiento`, { idAsiento }); }
  equipajes(id: number) { return this.http.get<Equipaje[]>(`${this.base}/reservas/${id}/equipajes`); }
  checkin(id: number) { return this.http.post<Checkin>(`${this.base}/reservas/${id}/checkin`, {}); }
  pase(id: number, vuelo: number) { return this.http.get<PaseAbordar>(`${this.base}/reservas/${id}/vuelos/${vuelo}/pase-abordar`); }
  notificaciones(pagina = 0) { return this.http.get<Pagina<Notificacion>>(`${this.base}/notificaciones`, { params: { pagina, tamano: 20 } }); }
  leerNotificacion(id: number) { return this.http.patch<Notificacion>(`${this.base}/notificaciones/${id}/leida`, {}); }
}
