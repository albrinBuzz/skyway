import { Injectable } from '@angular/core';
import { VistaTicket } from './models';

const OFFLINE_PASSES_KEY = 'skyway_offline_passes_v1';
const MAX_CACHED_PASSES = 6;

export interface CachedBoardingPass {
  key: string;
  idReserva: number;
  idVuelo: number;
  rut: string | null;
  savedAt: number;
  data: VistaTicket;
  qrDataUrl?: string;
  barcodeUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class OfflinePassService {
  buildKey(idReserva: number, idVuelo: number, rut?: string | null): string {
    return `${idReserva}_${idVuelo}_${(rut || 'TITULAR').trim().toUpperCase()}`;
  }

  save(
    idReserva: number,
    idVuelo: number,
    rut: string | null,
    data: VistaTicket,
    qrDataUrl?: string,
    barcodeUrl?: string
  ): void {
    try {
      const list = this.listAll();
      const key = this.buildKey(idReserva, idVuelo, rut);
      const entry: CachedBoardingPass = {
        key,
        idReserva,
        idVuelo,
        rut: rut ? rut.trim() : null,
        savedAt: Date.now(),
        data,
        qrDataUrl: qrDataUrl || undefined,
        barcodeUrl: barcodeUrl || undefined
      };
      const filtered = list.filter(x => x.key !== key);
      filtered.unshift(entry);
      localStorage.setItem(OFFLINE_PASSES_KEY, JSON.stringify(filtered.slice(0, MAX_CACHED_PASSES)));
    } catch {
      // Ignorar si almacenamiento no está disponible
    }
  }

  get(idReserva: number, idVuelo: number, rut?: string | null): CachedBoardingPass | null {
    const key = this.buildKey(idReserva, idVuelo, rut);
    return this.listAll().find(x => x.key === key) ?? null;
  }

  latest(): CachedBoardingPass | null {
    const all = this.listAll();
    return all.length ? all[0] : null;
  }

  listAll(): CachedBoardingPass[] {
    try {
      const raw = localStorage.getItem(OFFLINE_PASSES_KEY);
      if (!raw) return [];
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
}
