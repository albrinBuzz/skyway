package com.SkyWay.mobile.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Contratos de la API: campos V3 en camelCase y composiciones de lectura. */
public final class MobileDtos {
    private MobileDtos() {}
    public record LoginRequest(@NotBlank @Email @Size(max=100) String correoElectronico,
                               @NotBlank @Size(max=72) String contrasena,
                               Boolean recordarSesion) {
        public LoginRequest(String correoElectronico, String contrasena) {
            this(correoElectronico, contrasena, false);
        }
    }
    public record PnrLookupRequest(@NotBlank @Size(max=20) String codigoPnr,
                                   @NotBlank @Size(max=100) String apellido) {}
    public record ConsultaPnrResponse(
            Integer idReserva,
            String codigoPnr,
            String estadoReserva,
            String nombrePasajero,
            String codigoIataOrigen,
            String codigoIataDestino,
            String ciudadOrigen,
            String ciudadDestino,
            LocalDateTime proximaSalida,
            String numeroVuelo,
            String asiento,
            String puerta,
            String terminal,
            boolean checkinRealizado,
            String qrContenido,
            String codigoIataBcbp,
            String barcodeBase64) {}
    public record DiaTarifaFlexible(LocalDate fecha, int disponibles, BigDecimal precioMinimo) {}
    public record AcompananteSolicitud(
            @NotBlank @Size(max=20) String rut,
            @NotBlank @Size(max=100) String nombre,
            @NotBlank @Size(max=100) String apellido,
            LocalDate fechaNacimiento,
            @Size(max=50) String nacionalidad) {}
    public record RegistroRequest(
            @NotBlank @Size(max=20) String rut,
            @NotBlank @Size(max=255) String nombre,
            @NotBlank @Size(max=255) String apellido,
            @NotBlank @Email @Size(max=100) String correoElectronico,
            @NotBlank @Size(min=6, max=72) String contrasena,
            @NotBlank @Size(max=255) String telefono,
            @NotNull LocalDate fechaNacimiento,
            @Size(max=20) String documentoIdentidad,
            @Size(max=50) String tipoDocumento,
            @Size(max=50) String nacionalidad) {}
    public record CambioAsientoRequest(
            @NotNull @Positive Integer idAsiento,
            Integer idMetodoPago,
            String urlRetornoBase) {
        public CambioAsientoRequest(Integer idAsiento) {
            this(idAsiento, 1, null);
        }
        public CambioAsientoRequest(Integer idAsiento, Integer idMetodoPago) {
            this(idAsiento, idMetodoPago, null);
        }
    }
    public record ConfirmarPagoCambioAsientoRequest(
            @NotBlank String ordenCompra,
            @NotBlank String pasarela,
            @NotBlank String token) {}
    public record ActualizarCorreoRequest(
            @NotBlank @Email @Size(max=100) String correoElectronico,
            @NotBlank @Size(max=72) String contrasenaActual) {}
    public record CambiarContrasenaRequest(
            @NotBlank @Size(max=72) String contrasenaActual,
            @NotBlank @Size(min=8,max=72) String nuevaContrasena) {}
    public record ItemEquipajeSolicitud(
            Integer idTipoEquipaje,
            String tipo,
            BigDecimal peso,
            String dimensiones,
            Integer precio) {}
    public record TipoEquipajeCatalogo(
            Integer idTipo,
            String tipo,
            String nombre,
            String descripcion,
            Integer precio,
            Integer pesoMaximo,
            String dimensiones,
            Integer maximoPermitido) {}
    public record AsientoSeleccionadoSolicitud(
            @NotNull Integer idVuelo,
            @NotNull Integer idAsiento,
            String rutPasajero) {
        public AsientoSeleccionadoSolicitud(Integer idVuelo, Integer idAsiento) {
            this(idVuelo, idAsiento, null);
        }
    }
    public record CrearReservaRequest(
            @NotNull @Positive Integer idItinerario,
            @NotNull @Positive Integer idItinerarioTarifa,
            Integer idMetodoPago,
            List<ItemEquipajeSolicitud> equipaje,
            List<AsientoSeleccionadoSolicitud> asientos,
            Integer idItinerarioRegreso,
            Integer idItinerarioTarifaRegreso,
            List<AcompananteSolicitud> acompanantes) {
        public CrearReservaRequest(Integer idItinerario, Integer idItinerarioTarifa) {
            this(idItinerario, idItinerarioTarifa, 1, List.of(), List.of(), null, null, List.of());
        }
        public CrearReservaRequest(Integer idItinerario, Integer idItinerarioTarifa, Integer idMetodoPago, List<ItemEquipajeSolicitud> equipaje) {
            this(idItinerario, idItinerarioTarifa, idMetodoPago, equipaje, List.of(), null, null, List.of());
        }
        public CrearReservaRequest(Integer idItinerario, Integer idItinerarioTarifa, Integer idMetodoPago,
                                   List<ItemEquipajeSolicitud> equipaje, List<AsientoSeleccionadoSolicitud> asientos) {
            this(idItinerario, idItinerarioTarifa, idMetodoPago, equipaje, asientos, null, null, List.of());
        }
    }
    public record IniciarPagoReservaRequest(
            @NotNull @Positive Integer idItinerario,
            @NotNull @Positive Integer idItinerarioTarifa,
            Integer idMetodoPago,
            List<ItemEquipajeSolicitud> equipaje,
            List<AsientoSeleccionadoSolicitud> asientos,
            String urlRetornoBase,
            Integer idItinerarioRegreso,
            Integer idItinerarioTarifaRegreso,
            List<AcompananteSolicitud> acompanantes) {
        public IniciarPagoReservaRequest(Integer idItinerario, Integer idItinerarioTarifa, Integer idMetodoPago,
                                         List<ItemEquipajeSolicitud> equipaje, List<AsientoSeleccionadoSolicitud> asientos,
                                         String urlRetornoBase) {
            this(idItinerario, idItinerarioTarifa, idMetodoPago, equipaje, asientos, urlRetornoBase, null, null, List.of());
        }
    }
    public record IniciarPagoReservaResponse(
            String ordenCompra,
            String pasarela,
            String redirectUrl) {}
    public record ConfirmarPagoReservaRequest(
            @NotBlank String ordenCompra,
            @NotBlank String pasarela,
            @NotBlank String token,
            CrearReservaRequest reserva) {}
    public record AsientoVueloDisponible(
            Integer idAsiento,
            String numeroAsiento,
            Integer fila,
            String letra,
            Integer idClase,
            String clase,
            Integer precio,
            String estado,
            Boolean esPasillo,
            Boolean esEmergencia) {}
    public record VueloConAsientos(
            Integer idVuelo,
            String numeroVuelo,
            String origen,
            String destino,
            LocalDateTime horaSalida,
            LocalDateTime horaLlegada,
            String nombreModeloAvion,
            boolean avionDoblePasillo,
            List<ConfiguracionCabinaItemDto> configuracionesCabina,
            List<AsientoVueloDisponible> asientos) {}
    public record UsuarioDto(String rut, String nombre, String apellido, String correoElectronico) {}
    public record PasajeroDto(String rut, String tipoDocumento, String numeroDocumento,
                             LocalDate fechaNacimiento, String nacionalidad) {}
    public record Perfil(UsuarioDto usuario, PasajeroDto pasajero) {}
    public record LoginResponse(String accessToken, String tokenType, long expiresIn, Perfil perfil) {}
    public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {
        public static <T> Pagina<T> of(List<T> items, int page, int size, long total) {
            return new Pagina<>(items, page, size, total, (int)((total + size - 1) / size));
        }
    }
    public record EstadoReservaDto(Integer idEstadoReserva, String descripcion) {}
    public record EstadoVueloDto(Integer idEstadoVuelo, String estado, String descripcion) {}
    public record AeropuertoDto(Integer idAeropuerto, String nombreAeropuerto, String codigoIata,
                                String ciudad, String pais, Double latitud, Double longitud) {}
    public record AeropuertoCatalogo(Integer idAeropuerto, String codigoIata, String nombreAeropuerto, String ciudad) {}
    public record TarifaDisponible(Integer idItinerarioTarifa, Integer idTarifa, String nombre, BigDecimal precio) {}
    public record ItinerarioDisponible(Integer idItinerario, String origen, String destino,
                                       String ciudadSalida, String ciudadLlegada,
                                       LocalDateTime horaSalida, LocalDateTime horaLlegada,
                                       Integer numeroEscalas, String duracion, Integer precioBase,
                                       List<TarifaDisponible> tarifas) {}
    public record ReservaCreada(
            Integer idReserva,
            String codigoPnr,
            String estado,
            BigDecimal total,
            String metodoPago) {
        public ReservaCreada(Integer idReserva, String estado, BigDecimal total) {
            this(idReserva, null, estado, total, null);
        }
    }
    public record PuertaDto(Integer idPuerta, String codigoPuerta, String terminal, Integer idAeropuerto) {}
    public record SegmentoDto(Integer idSegmento, Integer ordenSegmento, LocalDateTime horaSalida,
                              LocalDateTime horaLlegada, AeropuertoDto origen, AeropuertoDto destino,
                              PuertaDto puerta) {}
    public record AsientoAsignado(Integer idReservaAsiento, Integer idAsiento, String numeroAsiento,
                                  Integer idClase, String clase, Integer precio) {
        public AsientoAsignado(Integer idReservaAsiento, Integer idAsiento, String numeroAsiento,
                               Integer idClase, String clase) {
            this(idReservaAsiento, idAsiento, numeroAsiento, idClase, clase, null);
        }
    }
    public record VueloDto(Integer idVuelo, String numeroVuelo, LocalDateTime fechaHoraSalida,
                           LocalDateTime fechaHoraLlegada, Integer idAvion, EstadoVueloDto estado,
                           List<SegmentoDto> segmentos, AsientoAsignado asiento, String modeloAvion) {}
    public record ConexionDto(Integer idItinerarioVuelo, Integer orden, String tiempoEspera,
                              String tipoConexion, VueloDto vuelo) {}
    public record CaracteristicaDto(Integer idTarifaCaracteristica, Integer idCaracteristica,
                                    String nombre, String descripcion, String tipoDato,
                                    String valor, Boolean valorBool, Integer valorInt) {}
    public record TarifaDto(Integer idItinerarioTarifa, Integer idTarifa, String nombre,
                            BigDecimal precio, List<CaracteristicaDto> caracteristicas) {}
    public record ItinerarioDto(Integer idReservaItinerario, Integer idItinerario,
                                LocalDateTime horaSalida, LocalDateTime horaLlegada,
                                Integer numeroEscalas, Integer precioBase, AeropuertoDto origen,
                                AeropuertoDto destino, TarifaDto tarifa, List<ConexionDto> vuelos) {}
    public record EquipajeDto(Integer idEquipaje, BigDecimal peso, String dimensiones, String tipo,
                             Integer idTipo, String nombreTipo) {}
    public record CheckinDto(Integer idCheckin, Integer idReserva, LocalDateTime fechaHora, String metodo) {}
    public record ResumenReserva(Integer idReserva, LocalDateTime fechaReserva, EstadoReservaDto estadoReserva,
                                 BigDecimal total, boolean activa, LocalDateTime proximaSalida,
                                 String codigoIataOrigen, String codigoIataDestino,
                                 boolean checkinRealizado) {
        public ResumenReserva(Integer idReserva, LocalDateTime fechaReserva, EstadoReservaDto estadoReserva,
                              BigDecimal total, boolean activa, LocalDateTime proximaSalida,
                              String codigoIataOrigen, String codigoIataDestino) {
            this(idReserva, fechaReserva, estadoReserva, total, activa, proximaSalida, codigoIataOrigen, codigoIataDestino, false);
        }
    }
    public record DetalleReserva(Integer idReserva, LocalDateTime fechaReserva, EstadoReservaDto estadoReserva,
                                 BigDecimal total, boolean titular, List<ItinerarioDto> itinerarios,
                                 List<EquipajeDto> equipajes, CheckinDto checkin,
                                 boolean checkinDisponible, LocalDateTime fechaAperturaCheckin,
                                 LocalDateTime fechaCierreCheckin, String estadoVentanaCheckin,
                                 String mensajeCheckin) {
        public DetalleReserva(Integer idReserva, LocalDateTime fechaReserva, EstadoReservaDto estadoReserva,
                              BigDecimal total, boolean titular, List<ItinerarioDto> itinerarios,
                              List<EquipajeDto> equipajes, CheckinDto checkin) {
            this(idReserva, fechaReserva, estadoReserva, total, titular, itinerarios, equipajes, checkin,
                    false, null, null, "NO_DISPONIBLE", null);
        }
    }
    public record ConfiguracionCabinaItemDto(Integer idConfiguracion, String clase,
                                             Integer filaInicio, Integer filaFin,
                                             String distribucionColumnas, String letrasColumnas,
                                             Boolean esSalidaEmergencia) {}

    public record AsientoDto(Integer idAsiento, String numeroAsiento, Integer idClase, String clase,
                             Integer precio, String estado, boolean seleccionable,
                             Integer diferenciaPrecio, Integer cargoGestion, Integer totalPagar,
                             boolean esUpgrade, Integer fila, String letra,
                             Boolean esPasillo, Boolean esEmergencia) {
        public AsientoDto(Integer idAsiento, String numeroAsiento, Integer idClase, String clase,
                          Integer precio, String estado, boolean seleccionable,
                          Integer diferenciaPrecio, Integer cargoGestion, Integer totalPagar,
                          boolean esUpgrade) {
            this(idAsiento, numeroAsiento, idClase, clase, precio, estado, seleccionable,
                 diferenciaPrecio, cargoGestion, totalPagar, esUpgrade, null, null, false, false);
        }

        public AsientoDto(Integer idAsiento, String numeroAsiento, Integer idClase, String clase,
                          Integer precio, String estado, boolean seleccionable) {
            this(idAsiento, numeroAsiento, idClase, clase, precio, estado, seleccionable, 0, 0, 0, false, null, null, false, false);
        }
    }
    public record MapaAsientos(Integer idReserva, Integer idVuelo, boolean cambioPermitido, String motivo,
                               AsientoAsignado asientoActual, Integer precioActual,
                               String nombreModeloAvion, boolean avionDoblePasillo,
                               List<ConfiguracionCabinaItemDto> configuracionesCabina,
                               List<AsientoDto> asientos,
                               String ordenCompra, String redirectUrl) {
        public MapaAsientos(Integer idReserva, Integer idVuelo, boolean cambioPermitido, String motivo,
                            AsientoAsignado asientoActual, Integer precioActual,
                            String nombreModeloAvion, boolean avionDoblePasillo,
                            List<ConfiguracionCabinaItemDto> configuracionesCabina,
                            List<AsientoDto> asientos) {
            this(idReserva, idVuelo, cambioPermitido, motivo, asientoActual, precioActual,
                 nombreModeloAvion, avionDoblePasillo, configuracionesCabina, asientos, null, null);
        }

        public MapaAsientos(Integer idReserva, Integer idVuelo, boolean cambioPermitido, String motivo,
                            AsientoAsignado asientoActual, Integer precioActual, List<AsientoDto> asientos) {
            this(idReserva, idVuelo, cambioPermitido, motivo, asientoActual, precioActual, null, false, List.of(), asientos, null, null);
        }

        public MapaAsientos(Integer idReserva, Integer idVuelo, boolean cambioPermitido, String motivo,
                           AsientoAsignado asientoActual, List<AsientoDto> asientos) {
            this(idReserva, idVuelo, cambioPermitido, motivo, asientoActual, null, null, false, List.of(), asientos, null, null);
        }
    }
    public record NotificacionDto(Integer idNotificacion, String titulo, String mensaje,
                                  Boolean leido, LocalDateTime fecha) {}
    public record ProximoVuelo(Integer idReserva, VueloDto vuelo) {}
    public record Inicio(Perfil perfil, ProximoVuelo proximoVuelo, long notificacionesSinLeer) {}
    public record PaseAbordar(CheckinDto checkin, UsuarioDto pasajero, VueloDto vuelo,
                             String qrContenido, boolean simulacion,
                             String codigoPnr, String codigoIataBcbp, String barcodeBase64) {
        public PaseAbordar(CheckinDto checkin, UsuarioDto pasajero, VueloDto vuelo,
                           String qrContenido, boolean simulacion) {
            this(checkin, pasajero, vuelo, qrContenido, simulacion, null, qrContenido, null);
        }
    }
    public record ApiError(String codigo, String mensaje, LocalDateTime fecha) {}
}
