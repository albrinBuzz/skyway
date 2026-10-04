import { HttpErrorResponse } from '@angular/common/http';
import { Caracteristica, DetalleReserva, Vuelo } from './models';
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'No pudimos conectar. Revisa tu conexión e inténtalo nuevamente.';
    if (typeof error.error?.mensaje === 'string') return error.error.mensaje;
  }
  return 'No se pudo completar la operación. Inténtalo nuevamente.';
}
export function featureValue(c: Caracteristica): string {
  if (c.tipoDato === 'boolean') return c.valorBool === null ? 'Sin especificar' : c.valorBool ? 'Sí' : 'No';
  if (c.tipoDato === 'int') return c.valorInt === null ? 'Sin especificar' : String(c.valorInt);
  return c.valor ?? 'Sin especificar';
}
export function flights(d: DetalleReserva): Vuelo[] {
  return [...new Map(d.itinerarios.flatMap(i => i.vuelos.map(c => [c.vuelo.idVuelo, c.vuelo] as const))).values()];
}
