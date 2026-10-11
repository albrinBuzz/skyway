package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.PasajeroReserva.domain.model.PasajeroReserva;
import com.SkyWay.modules.PasajeroReserva.domain.repository.PasajeroReservaRepository;
import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.estadoreserva.domain.repository.EstadoReservaRepository;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.repository.PasajeroRepository;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.repository.ReservaItinerarioRepository;
import com.SkyWay.modules.tarifaItinerario.domain.repository.ItinerarioTarifaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class MobileBookingService {
    private final MobileAuthService auth;
    private final AeropuertoRepository aeropuertos;
    private final ItinerarioService itinerarios;
    private final ItinerarioTarifaRepository tarifas;
    private final ReservaRepository reservas;
    private final ReservaItinerarioRepository reservaItinerarios;
    private final PasajeroRepository pasajeros;
    private final PasajeroReservaRepository pasajerosReservas;
    private final EstadoReservaRepository estados;
    private final Clock clock;

    public MobileBookingService(
            MobileAuthService auth,
            AeropuertoRepository aeropuertos,
            ItinerarioService itinerarios,
            ItinerarioTarifaRepository tarifas,
            ReservaRepository reservas,
            ReservaItinerarioRepository reservaItinerarios,
            PasajeroRepository pasajeros,
            PasajeroReservaRepository pasajerosReservas,
            EstadoReservaRepository estados,
            Clock clock) {
        this.auth = auth;
        this.aeropuertos = aeropuertos;
        this.itinerarios = itinerarios;
        this.tarifas = tarifas;
        this.reservas = reservas;
        this.reservaItinerarios = reservaItinerarios;
        this.pasajeros = pasajeros;
        this.pasajerosReservas = pasajerosReservas;
        this.estados = estados;
        this.clock = clock;
    }

    public List<AeropuertoCatalogo> aeropuertos(String rut) {
        auth.requirePassenger(rut);
        return aeropuertos.findAllConCoordenadas().stream()
                .map(a -> new AeropuertoCatalogo(
                        a.getIdAeropuerto(),
                        a.getCodigoIata(),
                        a.getNombreAeropuerto(),
                        a.getCiudad()))
                .sorted(Comparator.comparing(AeropuertoCatalogo::codigoIata,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public List<ItinerarioDisponible> buscar(String rut, String origen, String destino, LocalDate fecha) {
        auth.requirePassenger(rut);
        String from = normalizarIata(origen, "origen");
        String to = normalizarIata(destino, "destino");
        if (from.equals(to)) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "RUTA_INVALIDA", "El origen y el destino deben ser distintos.");
        }
        if (fecha == null || fecha.isBefore(LocalDate.now(clock))) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "FECHA_INVALIDA", "La fecha de viaje no puede estar en el pasado.");
        }

        try {
            return itinerarios.buscarItinerarios(from, to, fecha.toString()).stream()
                    .map(dto -> {
                        var entity = itinerarios.findById(dto.getItinerario());
                        if (entity == null) return null;
                        var opciones = tarifas.findByItinerarioIdItinerario(entity.getIdItinerario()).stream()
                                .map(t -> new TarifaDisponible(
                                        t.getIdItinerarioTarifa(),
                                        t.getTarifa().getIdTarifa(),
                                        t.getTarifa().getNombre(),
                                        t.getPrecio()))
                                .sorted(Comparator.comparing(TarifaDisponible::precio))
                                .toList();
                        LocalDateTime salida = entity.getHoraSalida() == null ? null : entity.getHoraSalida().toLocalDateTime();
                        LocalDateTime llegada = entity.getHoraLlegada() == null ? null : entity.getHoraLlegada().toLocalDateTime();
                        return new ItinerarioDisponible(
                                entity.getIdItinerario(),
                                dto.getOrigen(), dto.getDestino(),
                                dto.getCiudadSalida(), dto.getCiudadLlegada(),
                                salida, llegada,
                                entity.getNumeroEscalas(), dto.getDuracion(),
                                entity.getPrecioBase(), opciones);
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (ParseException ex) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "FECHA_INVALIDA", "La fecha de viaje no tiene un formato válido.");
        }
    }

    @Transactional
    public ReservaCreada crear(String rut, CrearReservaRequest request) {
        auth.requirePassenger(rut);
        var pasajero = pasajeros.findById(rut).orElseThrow(MobileException::notFound);
        var itinerario = itinerarios.findById(request.idItinerario());
        if (itinerario == null) throw MobileException.notFound();

        var tarifa = tarifas.findById(request.idItinerarioTarifa()).orElseThrow(MobileException::notFound);
        if (tarifa.getItinerario() == null
                || !request.idItinerario().equals(tarifa.getItinerario().getIdItinerario())) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "TARIFA_INVALIDA", "La tarifa no pertenece al itinerario seleccionado.");
        }

        LocalDateTime salida = itinerario.getHoraSalida() == null ? null : itinerario.getHoraSalida().toLocalDateTime();
        if (salida == null || !salida.isAfter(LocalDateTime.now(clock))) {
            throw MobileException.conflict("El itinerario ya no está disponible para reservar.");
        }

        var estado = estados.findFirstByDescripcionIgnoreCase("Confirmada")
                .orElseThrow(() -> new MobileException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "ESTADO_RESERVA_NO_CONFIGURADO",
                        "No existe el estado Confirmada en la base de datos."));

        var reserva = new Reserva();
        reserva.setFechaReserva(Timestamp.valueOf(LocalDateTime.now(clock)));
        reserva.setEstadoReservaBean(estado);
        reserva.setPasajero(pasajero);
        reserva.setTotal(tarifa.getPrecio());
        reserva = reservas.saveAndFlush(reserva);

        var ri = new ReservaItinerario();
        ri.setReserva(reserva);
        ri.setItinerario(itinerario);
        ri.setItinerarioTarifa(tarifa);
        reservaItinerarios.save(ri);

        // Mantiene también la relación de pasajeros de la reserva para que todas las consultas móviles
        // y futuros flujos multi-pasajero trabajen sobre la misma estructura.
        if (pasajerosReservas.findByReservaAndPasajero(reserva, pasajero).isEmpty()) {
            var pr = new PasajeroReserva();
            pr.setReserva(reserva);
            pr.setPasajero(pasajero);
            pasajerosReservas.save(pr);
        }

        return new ReservaCreada(reserva.getIdReserva(), estado.getDescripcion(), reserva.getTotal());
    }

    private static String normalizarIata(String value, String campo) {
        String result = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!result.matches("[A-Z]{3}")) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "IATA_INVALIDO", "El aeropuerto de " + campo + " no es válido.");
        }
        return result;
    }
}
