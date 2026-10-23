export interface ParadaRuta {
  codigoIata: string;
  nombreAeropuerto: string;
  ciudad: string;
  pais: string;
  latitud: number;
  longitud: number;
  tipo: 'origen' | 'escala' | 'destino';
  colorHex: string;
  numeroVuelo?: string | null;
  tiempoEspera?: string | null;
  tipoConexion?: string | null;
}

export interface TramoRuta {
  origen: ParadaRuta;
  destino: ParadaRuta;
  numeroVuelo?: string | null;
  distanciaKm: number;
}

export interface RutaVuelo3D {
  idReserva: number;
  codigoOrigen: string;
  codigoDestino: string;
  ciudadOrigen: string;
  ciudadDestino: string;
  paisOrigen: string;
  paisDestino: string;
  paradas: ParadaRuta[];
  tramos: TramoRuta[];
  distanciaKm: number;
  duracionTotal: string;
  numEscalas: number;
  nombresEscalas: string[];
}
