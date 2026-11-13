import { AeropuertoCatalogo } from './models';

const DEFAULT_IATA_CITIES: Record<string, string> = {
  SCL: 'Santiago',
  JFK: 'Nueva York',
  MIA: 'Miami',
  MAD: 'Madrid',
  EZE: 'Buenos Aires',
  LIM: 'Lima',
  BOG: 'Bogotá',
  GRU: 'São Paulo',
  CUN: 'Cancún',
  LAX: 'Los Ángeles',
  MEX: 'Ciudad de México',
  ATL: 'Atlanta',
  MVD: 'Montevideo',
  PTY: 'Ciudad de Panamá',
  GIG: 'Río de Janeiro',
  LHR: 'Londres',
  CDG: 'París',
  BCN: 'Barcelona',
  FCO: 'Roma'
};

const dynamicIataCities = new Map<string, string>();

export function registrarAeropuertosIata(lista: AeropuertoCatalogo[] | null | undefined): void {
  if (!Array.isArray(lista)) return;
  for (const a of lista) {
    const code = (a?.codigoIata || '').trim().toUpperCase();
    const city = (a?.ciudad || '').trim();
    if (code && city) {
      dynamicIataCities.set(code, city);
    }
  }
}

export function ciudadPorIata(iata?: string | null): string {
  const code = (iata || '').trim().toUpperCase();
  if (!code) return 'Aeropuerto';
  return dynamicIataCities.get(code) || DEFAULT_IATA_CITIES[code] || code;
}
