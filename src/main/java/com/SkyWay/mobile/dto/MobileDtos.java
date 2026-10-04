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
                               @NotBlank @Size(max=72) String contrasena) {}
    public record CambioAsientoRequest(@NotNull @Positive Integer idAsiento) {}
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
    public record AeropuertoDto(Integer idAeropuerto, String nombreAeropuerto, String codigoIata) {}
    public record PuertaDto(Integer idPuerta, String codigoPuerta, String terminal, Integer idAeropuerto) {}
    public record SegmentoDto(Integer idSegmento, Integer ordenSegmento, LocalDateTime horaSalida,
                              LocalDateTime horaLlegada, AeropuertoDto origen, AeropuertoDto destino,
                              PuertaDto puerta) {}
    public record AsientoAsignado(Integer idReservaAsiento, Integer idAsiento, String numeroAsiento,
                                  Integer idClase, String clase) {}
    public record VueloDto(Integer idVuelo, String numeroVuelo, LocalDateTime fechaHoraSalida,
                           LocalDateTime fechaHoraLlegada, Integer idAvion, EstadoVueloDto estado,
                           List<SegmentoDto> segmentos, AsientoAsignado asiento) {}
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
                                 String codigoIataOrigen, String codigoIataDestino) {}
    public record DetalleReserva(Integer idReserva, LocalDateTime fechaReserva, EstadoReservaDto estadoReserva,
                                 BigDecimal total, boolean titular, List<ItinerarioDto> itinerarios,
                                 List<EquipajeDto> equipajes, CheckinDto checkin) {}
    public record AsientoDto(Integer idAsiento, String numeroAsiento, Integer idClase, String clase,
                             Integer precio, String estado, boolean seleccionable) {}
    public record MapaAsientos(Integer idReserva, Integer idVuelo, boolean cambioPermitido, String motivo,
                               AsientoAsignado asientoActual, List<AsientoDto> asientos) {}
    public record NotificacionDto(Integer idNotificacion, String titulo, String mensaje,
                                  Boolean leido, LocalDateTime fecha) {}
    public record ProximoVuelo(Integer idReserva, VueloDto vuelo) {}
    public record Inicio(Perfil perfil, ProximoVuelo proximoVuelo, long notificacionesSinLeer) {}
    public record PaseAbordar(CheckinDto checkin, UsuarioDto pasajero, VueloDto vuelo,
                             String qrContenido, boolean simulacion) {}
    public record ApiError(String codigo, String mensaje, LocalDateTime fecha) {}
}
