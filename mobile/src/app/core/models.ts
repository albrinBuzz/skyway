export interface Usuario { rut: string; nombre: string; apellido: string; correoElectronico: string | null; }
export interface Pasajero { rut: string; tipoDocumento: string | null; numeroDocumento: string | null; fechaNacimiento: string | null; nacionalidad: string | null; }
export interface Perfil { usuario: Usuario; pasajero: Pasajero; }
export interface LoginResponse { accessToken: string; tokenType: string; expiresIn: number; perfil: Perfil; }
export interface Pagina<T> { contenido: T[]; pagina: number; tamano: number; totalElementos: number; totalPaginas: number; }
export interface EstadoReserva { idEstadoReserva: number | null; descripcion: string | null; }
export interface EstadoVuelo { idEstadoVuelo: number | null; estado: string | null; descripcion: string | null; }
export interface Aeropuerto { idAeropuerto: number; nombreAeropuerto: string; codigoIata: string; }
export interface Puerta { idPuerta: number; codigoPuerta: string; terminal: string | null; idAeropuerto: number | null; }
export interface Segmento { idSegmento: number; ordenSegmento: number | null; horaSalida: string | null; horaLlegada: string | null; origen: Aeropuerto | null; destino: Aeropuerto | null; puerta: Puerta | null; }
export interface AsientoAsignado { idReservaAsiento: number; idAsiento: number; numeroAsiento: string; idClase: number | null; clase: string | null; }
export interface Vuelo { idVuelo: number; numeroVuelo: string | null; fechaHoraSalida: string | null; fechaHoraLlegada: string | null; idAvion: number | null; estado: EstadoVuelo; segmentos: Segmento[]; asiento: AsientoAsignado | null; }
export interface Conexion { idItinerarioVuelo: number; orden: number | null; tiempoEspera: string | null; tipoConexion: string | null; vuelo: Vuelo; }
export interface Caracteristica { idTarifaCaracteristica: number; idCaracteristica: number; nombre: string; descripcion: string | null; tipoDato: 'boolean' | 'int' | 'text' | null; valor: string | null; valorBool: boolean | null; valorInt: number | null; }
export interface Tarifa { idItinerarioTarifa: number; idTarifa: number; nombre: string; precio: number; caracteristicas: Caracteristica[]; }
export interface Itinerario { idReservaItinerario: number; idItinerario: number; horaSalida: string | null; horaLlegada: string | null; numeroEscalas: number | null; precioBase: number | null; origen: Aeropuerto | null; destino: Aeropuerto | null; tarifa: Tarifa; vuelos: Conexion[]; }
export interface Equipaje { idEquipaje: number; peso: number; dimensiones: string; tipo: string; idTipo: number | null; nombreTipo: string | null; }
export interface Checkin { idCheckin: number; idReserva: number; fechaHora: string | null; metodo: 'Web' | 'App' | 'Mostrador' | null; }
export interface ResumenReserva { idReserva: number; fechaReserva: string; estadoReserva: EstadoReserva; total: number | null; activa: boolean; proximaSalida: string | null; codigoIataOrigen: string | null; codigoIataDestino: string | null; }
export interface DetalleReserva { idReserva: number; fechaReserva: string; estadoReserva: EstadoReserva; total: number | null; titular: boolean; itinerarios: Itinerario[]; equipajes: Equipaje[]; checkin: Checkin | null; }
export interface Asiento { idAsiento: number; numeroAsiento: string; idClase: number | null; clase: string | null; precio: number | null; estado: 'libre' | 'ocupado' | 'seleccionado' | 'sin_precio'; seleccionable: boolean; }
export interface MapaAsientos { idReserva: number; idVuelo: number; cambioPermitido: boolean; motivo: string | null; asientoActual: AsientoAsignado | null; asientos: Asiento[]; }
export interface Notificacion { idNotificacion: number; titulo: string | null; mensaje: string | null; leido: boolean | null; fecha: string | null; }
export interface Inicio { perfil: Perfil; proximoVuelo: { idReserva: number; vuelo: Vuelo } | null; notificacionesSinLeer: number; }
export interface PaseAbordar { checkin: Checkin; pasajero: Usuario; vuelo: Vuelo; qrContenido: string; simulacion: boolean; }
