package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Un ticket individual por pasajero y vuelo. No acepta el RUT solicitante desde el cliente:
 * el solicitante SIEMPRE proviene del JWT verificado por Spring Security.
 *
 * Se integra a la API V3 existente SIN cambiar el esquema ni sobrescribir fn_getticket.
 */
@Service
@Transactional(readOnly = true)
public class MobileGroupTicketsService {
    @PersistenceContext private EntityManager em;
    private final MobileReadService read;
    private final MobileOperationsService operations;
    private final MobileAuthService auth;
    private final ReservaRepository reservas;

    public MobileGroupTicketsService(MobileReadService read,
                                     MobileOperationsService operations,
                                     MobileAuthService auth,
                                     ReservaRepository reservas) {
        this.read = read;
        this.operations = operations;
        this.auth = auth;
        this.reservas = reservas;
    }

    public record PasajeroTicket(String rut, String nombre, String apellido,
                                 boolean titular, AsientoAsignado asiento) {}

    public record VistaTicket(UsuarioDto pasajero, VueloDto vuelo, CheckinDto checkin,
                              List<EquipajeDto> equipajes, String qrContenido,
                              boolean simulacion, String codigoPnr) {}

    private DetalleReserva autorizarReservaYVuelo(String jwtRut, int reserva, int vuelo) {
        // read.detalle valida la pertenencia a la reserva antes de cargar información.
        var detail = read.detalle(jwtRut, reserva);
        MobileReadService.vuelo(detail, vuelo); // Vuelo obligatoriamente asociado a ESTA reserva.
        return detail;
    }

    private boolean miembroDeReserva(int reserva, String rut) {
        Number found = (Number) em.createNativeQuery("""
            SELECT count(*) FROM reserva r
            WHERE r.id_reserva = :reserva
              AND (r.rut_pasajero = :rut
                   OR EXISTS (SELECT 1 FROM pasajero_reserva pr
                              WHERE pr.id_reserva = r.id_reserva AND pr.rut = :rut)
                   OR EXISTS (SELECT 1 FROM reserva_asiento ra
                              WHERE ra.id_reserva = r.id_reserva AND ra.rut = :rut))
            """).setParameter("reserva", reserva).setParameter("rut", rut).getSingleResult();
        return found.longValue() > 0;
    }

    private DetalleReserva autorizarPersona(String jwtRut, int reserva, int vuelo, String pasajeroRut) {
        var detail = autorizarReservaYVuelo(jwtRut, reserva, vuelo);
        // Solo el titular puede consultar/modificar tickets de compañeros. El acompañante ve el propio.
        if (!detail.titular() && !jwtRut.equals(pasajeroRut)) {
            throw new MobileException(HttpStatus.FORBIDDEN, "SIN_PERMISO_TICKET",
                    "Solo el titular puede administrar el ticket de otro pasajero.");
        }
        if (!miembroDeReserva(reserva, pasajeroRut)) {
            throw MobileException.notFound();
        }
        return detail;
    }

    private AsientoAsignado asientoAsignado(int reserva, int vuelo, String rut) {
        List<Object[]> seats = reservas.mobileAsignados(reserva, rut).stream()
                .filter(row -> Objects.equals(MobileReadService.number(row[0]), vuelo)).toList();
        if (seats.size() > 1) {
            throw MobileException.conflict("Hay más de un asiento para este pasajero y vuelo. Revisa la reserva.");
        }
        if (seats.isEmpty()) return null;
        Object[] row = seats.get(0);
        return new AsientoAsignado(
                MobileReadService.number(row[1]), MobileReadService.number(row[2]),
                MobileReadService.text(row[3]), MobileReadService.number(row[4]),
                MobileReadService.text(row[5]));
    }

    @SuppressWarnings("unchecked")
    public List<PasajeroTicket> listar(String jwtRut, int reserva, int vuelo) {
        var detail = autorizarReservaYVuelo(jwtRut, reserva, vuelo);
        // UNION evita duplicar un pasajero presente en pasajero_reserva y reserva_asiento.
        // La asociación de asientos se filtra estrictamente por reserva Y vuelo.
        List<Object[]> persons = em.createNativeQuery("""
            WITH personas AS (
               SELECT pr.rut FROM pasajero_reserva pr WHERE pr.id_reserva = :reserva
               UNION
               SELECT ra.rut FROM reserva_asiento ra WHERE ra.id_reserva = :reserva AND ra.id_vuelo = :vuelo
               UNION
               SELECT r.rut_pasajero AS rut FROM reserva r WHERE r.id_reserva = :reserva
            )
            SELECT u.rut, u.nombre, u.apellido, r.rut_pasajero
            FROM personas p
            JOIN usuario u ON u.rut = p.rut
            JOIN reserva r ON r.id_reserva = :reserva
            ORDER BY CASE WHEN u.rut = r.rut_pasajero THEN 0 ELSE 1 END,
                     u.nombre, u.apellido, u.rut
            """).setParameter("reserva", reserva).setParameter("vuelo", vuelo).getResultList();
        List<PasajeroTicket> result = new ArrayList<>();
        for (Object[] person : persons) {
            String rut = MobileReadService.text(person[0]);
            if (!detail.titular() && !jwtRut.equals(rut)) continue;
            result.add(new PasajeroTicket(rut, MobileReadService.text(person[1]),
                    MobileReadService.text(person[2]), Objects.equals(rut, person[3]),
                    asientoAsignado(reserva, vuelo, rut)));
        }
        return result;
    }

    private String usuarioNombre(int reserva, String rut, boolean nombre) {
        Object value = em.createNativeQuery(nombre
                ? "SELECT u.nombre FROM usuario u JOIN reserva r ON r.id_reserva = :reserva WHERE u.rut = :rut"
                : "SELECT u.apellido FROM usuario u JOIN reserva r ON r.id_reserva = :reserva WHERE u.rut = :rut")
                .setParameter("reserva", reserva)
                .setParameter("rut", rut)
                .getResultStream().findFirst().orElse(null);
        return MobileReadService.text(value);
    }

    public VistaTicket vista(String jwtRut, int reserva, int vuelo, String pasajeroRut) {
        var detail = autorizarPersona(jwtRut, reserva, vuelo, pasajeroRut);
        var oldFlight = MobileReadService.vuelo(detail, vuelo);
        var seat = asientoAsignado(reserva, vuelo, pasajeroRut);
        // Vuelo inmutable, con el ASIENTO DEL PASAJERO ELEGIDO, nunca el del JWT.
        var flight = new VueloDto(oldFlight.idVuelo(), oldFlight.numeroVuelo(),
                oldFlight.fechaHoraSalida(), oldFlight.fechaHoraLlegada(), oldFlight.idAvion(),
                oldFlight.estado(), oldFlight.segmentos(), seat, oldFlight.modeloAvion());
        var baggage = reservas.mobileEquipajes(reserva, pasajeroRut).stream().map(row ->
                new EquipajeDto(MobileReadService.number(row[0]), (BigDecimal) row[1],
                        MobileReadService.text(row[2]), MobileReadService.text(row[3]),
                        MobileReadService.number(row[4]), MobileReadService.text(row[5]))).toList();

        var checkin = detail.checkin();

        // codigo_reserva existe en versiones nuevas de SkyWay; el dump V3 antiguo no la incluye.
        // La comprobación de metadatos evita errores cuando se instala el módulo sobre V3.
        Number hasPnrColumn = (Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'reserva'
                  AND column_name = 'codigo_reserva'
                """).getSingleResult();
        String codigoPnr = null;
        if (hasPnrColumn.intValue() > 0) {
            Object value = em.createNativeQuery("""
                    SELECT codigo_reserva FROM reserva WHERE id_reserva = :reserva
                    """).setParameter("reserva", reserva).getSingleResult();
            codigoPnr = MobileReadService.text(value);
        }
        String qr = null;
        if (checkin != null && seat != null) {
            // Compatible con el formato de codigo_qr en fn_getticket cuando hay PNR.
            if (codigoPnr != null && !codigoPnr.isBlank() && flight.numeroVuelo() != null) {
                qr = "SKYWAY-" + codigoPnr + "-" + flight.numeroVuelo() + "-"
                        + seat.numeroAsiento() + "-" + pasajeroRut;
            } else {
                qr = String.format("SKYWAY|CH:%d|RES:%d|FL:%d|SEAT:%s|RUT:%s",
                        checkin.idCheckin(), reserva, vuelo, seat.numeroAsiento(), pasajeroRut);
            }
        }
        return new VistaTicket(new UsuarioDto(pasajeroRut, usuarioNombre(reserva, pasajeroRut, true),
                usuarioNombre(reserva, pasajeroRut, false), null), flight, checkin,
                baggage, qr, true, codigoPnr);
    }

    public MapaAsientos mapa(String jwtRut, int reserva, int vuelo, String pasajeroRut) {
        autorizarPersona(jwtRut, reserva, vuelo, pasajeroRut);
        return operations.mapa(pasajeroRut, reserva, vuelo);
    }

    @Transactional
    public MapaAsientos cambiarAsiento(String jwtRut, int reserva, int vuelo,
                                       String pasajeroRut, int asientoId) {
        autorizarPersona(jwtRut, reserva, vuelo, pasajeroRut);
        // Reutiliza la lógica anterior: tarifa, clase, fecha, ocupación, locks y check-in.
        // NO hace UPDATE directo sobre reserva_asiento.
        return operations.cambiar(pasajeroRut, reserva, vuelo, asientoId);
    }
}
