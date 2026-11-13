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

export async function saveOrSharePdfBlob(blob: Blob, filename: string): Promise<void> {
  if (typeof navigator !== 'undefined' && typeof File !== 'undefined' && typeof navigator.share === 'function') {
    try {
      const file = new File([blob], filename, { type: 'application/pdf' });
      const canShare = typeof navigator.canShare === 'function' ? navigator.canShare({ files: [file] }) : false;
      if (canShare) {
        await navigator.share({ files: [file], title: filename });
        return;
      }
    } catch (err: unknown) {
      if (err instanceof DOMException && err.name === 'AbortError') return;
    }
  }

  if (typeof window === 'undefined' || typeof document === 'undefined') return;
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.rel = 'noopener';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  setTimeout(() => window.URL.revokeObjectURL(url), 15000);
}
