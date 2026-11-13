export interface Usuario {
  rut: string;
  nombre: string;
  apellido: string;
  correoElectronico: string | null;
}

export interface Pasajero {
  rut: string;
  tipoDocumento: string | null;
  numeroDocumento: string | null;
  fechaNacimiento: string | null;
  nacionalidad: string | null;
}

export interface Perfil {
  usuario: Usuario;
  pasajero: Pasajero;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  perfil: Perfil;
}

export interface RegistroRequest {
  rut: string;
  nombre: string;
  apellido: string;
  correoElectronico: string;
  contrasena: string;
  telefono: string;
  fechaNacimiento: string;
  documentoIdentidad?: string;
  tipoDocumento?: string;
  nacionalidad?: string;
}

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface EstadoReserva {
  idEstadoReserva: number | null;
  descripcion: string | null;
}

export interface EstadoVuelo {
  idEstadoVuelo: number | null;
  estado: string | null;
  descripcion: string | null;
}

export interface Aeropuerto {
  idAeropuerto: number;
  nombreAeropuerto: string;
  codigoIata: string;
  ciudad?: string | null;
  pais?: string | null;
  latitud?: number | null;
  longitud?: number | null;
}

export interface AeropuertoCatalogo extends Aeropuerto {
  ciudad: string | null;
}

export interface Puerta {
  idPuerta: number;
  codigoPuerta: string;
  terminal: string | null;
  idAeropuerto: number | null;
}

export interface Segmento {
  idSegmento: number;
  ordenSegmento: number | null;
  horaSalida: string | null;
  horaLlegada: string | null;
  origen: Aeropuerto | null;
  destino: Aeropuerto | null;
  puerta: Puerta | null;
}

export interface AsientoAsignado {
  idReservaAsiento: number;
  idAsiento: number;
  numeroAsiento: string;
  idClase: number | null;
  clase: string | null;
  precio?: number | null;
}

export interface Vuelo {
  modeloAvion: string | null;
  idVuelo: number;
  numeroVuelo: string | null;
  fechaHoraSalida: string | null;
  fechaHoraLlegada: string | null;
  idAvion: number | null;
  estado: EstadoVuelo;
  segmentos: Segmento[];
  asiento: AsientoAsignado | null;
}

export interface Conexion {
  idItinerarioVuelo: number;
  orden: number | null;
  tiempoEspera: string | null;
  tipoConexion: string | null;
  vuelo: Vuelo;
}

export interface Caracteristica {
  idTarifaCaracteristica: number;
  idCaracteristica: number;
  nombre: string;
  descripcion: string | null;
  tipoDato: 'boolean' | 'int' | 'text' | null;
  valor: string | null;
  valorBool: boolean | null;
  valorInt: number | null;
}

export interface Tarifa {
  idItinerarioTarifa: number;
  idTarifa: number;
  nombre: string;
  precio: number;
  caracteristicas: Caracteristica[];
}

export interface Itinerario {
  idReservaItinerario: number;
  idItinerario: number;
  horaSalida: string | null;
  horaLlegada: string | null;
  numeroEscalas: number | null;
  precioBase: number | null;
  origen: Aeropuerto | null;
  destino: Aeropuerto | null;
  tarifa: Tarifa;
  vuelos: Conexion[];
}

export interface Equipaje {
  idEquipaje: number;
  peso: number;
  dimensiones: string;
  tipo: string;
  idTipo: number | null;
  nombreTipo: string | null;
}

export interface Checkin {
  idCheckin: number;
  idReserva: number;
  fechaHora: string | null;
  metodo: 'Web' | 'App' | 'Mostrador' | null;
}

export interface ResumenReserva {
  idReserva: number;
  fechaReserva: string;
  estadoReserva: EstadoReserva;
  total: number | null;
  activa: boolean;
  proximaSalida: string | null;
  codigoIataOrigen: string | null;
  codigoIataDestino: string | null;
  codigoPnr?: string | null;
  esTitular?: boolean;
  checkinRealizado?: boolean;
  totalPasajeros?: number | null;
}

export interface DetalleReserva {
  idReserva: number;
  fechaReserva: string;
  estadoReserva: EstadoReserva;
  total: number | null;
  titular: boolean;
  codigoPnr?: string | null;
  itinerarios: Itinerario[];
  equipajes: Equipaje[];
  checkin: Checkin | null;
  checkinDisponible?: boolean;
  fechaAperturaCheckin?: string | null;
  fechaCierreCheckin?: string | null;
  estadoVentanaCheckin?: 'HABILITADO' | 'NO_DISPONIBLE_AUN' | 'CERRADO' | string;
  mensajeCheckin?: string | null;
}

export interface Asiento {
  idAsiento: number;
  numeroAsiento: string;
  idClase: number | null;
  clase: string | null;
  precio: number | null;
  estado: 'libre' | 'ocupado' | 'seleccionado' | 'sin_precio';
  seleccionable: boolean;
  diferenciaPrecio?: number;
  cargoGestion?: number;
  totalPagar?: number;
  esUpgrade?: boolean;
  fila?: number | null;
  letra?: string | null;
  esPasillo?: boolean;
  esEmergencia?: boolean;
}

export interface ConfiguracionCabinaItem {
  idConfiguracion: number;
  clase: string;
  filaInicio: number;
  filaFin: number;
  distribucionColumnas: string;
  letrasColumnas: string;
  esSalidaEmergencia: boolean;
}

export interface MapaAsientos {
  idReserva: number;
  idVuelo: number;
  cambioPermitido: boolean;
  motivo: string | null;
  asientoActual: AsientoAsignado | null;
  precioActual?: number | null;
  nombreModeloAvion?: string | null;
  avionDoblePasillo?: boolean;
  configuracionesCabina?: ConfiguracionCabinaItem[];
  asientos: Asiento[];
  ordenCompra?: string | null;
  redirectUrl?: string | null;
}

export interface Notificacion {
  idNotificacion: number;
  titulo: string | null;
  mensaje: string | null;
  leido: boolean | null;
  fecha: string | null;
}

export interface Inicio {
  perfil: Perfil;
  proximoVuelo: { idReserva: number; codigoPnr?: string | null; vuelo: Vuelo } | null;
  notificacionesSinLeer: number;
}

export interface PaseAbordar {
  checkin: Checkin;
  pasajero: Usuario;
  vuelo: Vuelo;
  qrContenido: string;
  simulacion: boolean;
  codigoPnr?: string | null;
  codigoIataBcbp?: string | null;
  barcodeBase64?: string | null;
}

// -------- Reserva de vuelos desde Inicio --------
export interface TarifaDisponible {
  idItinerarioTarifa: number;
  idTarifa: number;
  nombre: string;
  precio: number;
}

export interface ItinerarioDisponible {
  idItinerario: number;
  origen: string;
  destino: string;
  ciudadSalida: string;
  ciudadLlegada: string;
  horaSalida: string | null;
  horaLlegada: string | null;
  numeroEscalas: number | null;
  duracion: string | null;
  precioBase: number | null;
  tarifas: TarifaDisponible[];
}

export interface ItemEquipajeSolicitud {
  idTipoEquipaje: number;
  tipo: string;
  peso: number;
  dimensiones: string;
  precio: number;
  rutPasajero?: string;
}

export interface TipoEquipajeCatalogo {
  idTipo: number;
  tipo: string;
  nombre: string;
  descripcion: string;
  precio: number;
  pesoMaximo: number;
  dimensiones: string;
  maximoPermitido: number;
}

export interface AsientoVueloDisponible {
  idAsiento: number;
  numeroAsiento: string;
  fila: number;
  letra: string;
  idClase: number;
  clase: string;
  precio: number;
  estado: string; // 'disponible' | 'ocupado'
  esPasillo?: boolean;
  esEmergencia?: boolean;
}

export interface VueloConAsientos {
  idVuelo: number;
  numeroVuelo: string | null;
  origen: string;
  destino: string;
  horaSalida: string | null;
  horaLlegada: string | null;
  nombreModeloAvion: string | null;
  avionDoblePasillo: boolean;
  configuracionesCabina: ConfiguracionCabinaItem[];
  asientos: AsientoVueloDisponible[];
}

export interface AsientoSeleccionadoSolicitud {
  idVuelo: number;
  idAsiento: number;
  rutPasajero?: string;
}

export interface AcompananteSolicitud {
  rut: string;
  nombre: string;
  apellido: string;
  fechaNacimiento?: string;
  nacionalidad?: string;
}

export interface DiaTarifaFlexible {
  fecha: string;
  disponibles: number;
  precioMinimo: number | null;
}

export interface ConsultaPnrResponse {
  idReserva: number;
  codigoPnr: string;
  estadoReserva: string;
  nombrePasajero: string;
  codigoIataOrigen: string | null;
  codigoIataDestino: string | null;
  ciudadOrigen: string | null;
  ciudadDestino: string | null;
  proximaSalida: string | null;
  numeroVuelo: string | null;
  asiento: string | null;
  puerta: string | null;
  terminal: string | null;
  checkinRealizado: boolean;
  qrContenido: string | null;
  codigoIataBcbp: string | null;
  barcodeBase64: string | null;
}

export interface CrearReservaRequest {
  idItinerario: number;
  idItinerarioTarifa: number;
  idMetodoPago?: number;
  equipaje?: ItemEquipajeSolicitud[];
  asientos?: AsientoSeleccionadoSolicitud[];
  idItinerarioRegreso?: number;
  idItinerarioTarifaRegreso?: number;
  acompanantes?: AcompananteSolicitud[];
}

export interface ReservaCreada {
  idReserva: number;
  codigoPnr?: string | null;
  estado: string;
  total: number;
  metodoPago?: string | null;
}

export interface IniciarPagoReservaResponse {
  ordenCompra: string;
  pasarela: string;
  redirectUrl: string;
}


// Varios pasajeros pueden viajar en una misma reserva; cada uno tiene su propio asiento y pase.
export interface PasajeroTicket {
  rut: string;
  nombre: string;
  apellido: string;
  titular: boolean;
  asiento: AsientoAsignado | null;
}

/** El backend valida JWT + pertenencia a la reserva antes de entregar estos datos. */
export interface VistaTicket {
  pasajero: Usuario;
  vuelo: Vuelo;
  checkin: Checkin | null;
  equipajes: Equipaje[];
  qrContenido: string | null;
  simulacion: boolean;
  codigoPnr: string | null;
  codigoIataBcbp?: string | null;
  barcodeBase64?: string | null;
}
