package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MobileOperationsServiceTest {

    private ReservaRepository reservas;
    private MobileReadService readService;
    private Clock fixedClock;
    private MobileOperationsService operationsService;

    private DetalleReserva detalleActual;
    private final Instant nowInstant = Instant.parse("2026-11-09T12:00:00Z");
    private final ZoneId zone = ZoneId.of("UTC");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(nowInstant, zone);
        reservas = mock(ReservaRepository.class);

        readService = new MobileReadService(reservas, null, null, fixedClock) {
            @Override
            public DetalleReserva detalle(String rut, int id) {
                return detalleActual;
            }
        };

        var noopEncoder = new org.springframework.security.crypto.password.PasswordEncoder() {
            @Override public String encode(CharSequence rawPassword) { return ""; }
            @Override public boolean matches(CharSequence rawPassword, String encodedPassword) { return true; }
        };
        MobileAuthService auth = new MobileAuthService(null, null, noopEncoder, null, fixedClock, null, 7200L) {
            @Override
            public void requirePassenger(String rut) {}
        };

        operationsService = new MobileOperationsService(reservas, null, readService, auth, fixedClock);
    }

    private DetalleReserva crearDetalleReserva(LocalDateTime fechaSalidaVuelo, boolean conCheckin) {
        EstadoReservaDto estadoReserva = new EstadoReservaDto(1, "Confirmada");
        EstadoVueloDto estadoVuelo = new EstadoVueloDto(1, "Programado", "A tiempo");
        AsientoAsignado asientoActual = new AsientoAsignado(100, 1, "12A", 3, "Económica", 15000);

        VueloDto vuelo = new VueloDto(
                55, "SK-101", fechaSalidaVuelo, fechaSalidaVuelo.plusHours(2),
                1, estadoVuelo, List.of(), asientoActual, "A320"
        );

        ConexionDto conexion = new ConexionDto(1, 1, "0", "Directo", vuelo);
        CaracteristicaDto caracPermite = new CaracteristicaDto(1, 1, "Permite Cambios Asiento", "Cambios", "boolean", "true", true, null);
        CaracteristicaDto caracHoras = new CaracteristicaDto(2, 2, "Horas Minimas Cambio Asiento", "Anticipacion", "int", "72", null, 72);
        TarifaDto tarifa = new TarifaDto(1, 1, "Tarifa Light", new BigDecimal("50000"), List.of(caracPermite, caracHoras));

        ItinerarioDto itinerario = new ItinerarioDto(
                1, 1, fechaSalidaVuelo, fechaSalidaVuelo.plusHours(2), 0,
                50000, null, null, tarifa, List.of(conexion)
        );

        CheckinDto checkin = conCheckin ? new CheckinDto(10, 10, LocalDateTime.now(fixedClock), "App") : null;

        return new DetalleReserva(
                10, LocalDateTime.now(fixedClock).minusDays(5), estadoReserva,
                new BigDecimal("50000"), true, List.of(itinerario), List.of(), checkin
        );
    }

    @Test
    @DisplayName("Cálculo de upgrade y cargo 15%: calcula diferencia y recargo administrativo exactamente")
    void debeCalcularDiferenciaYCargoGestionParaUpgrade() {
        String rut = "11111111-1";
        int idReserva = 10;
        int idVuelo = 55;
        LocalDateTime salida = LocalDateTime.now(fixedClock).plusHours(80);

        detalleActual = crearDetalleReserva(salida, false);

        // Simulamos el catálogo de asientos devuelto por mobileMapa:
        // row: [seatId, numSeat, classId, className, price, state]
        List<Object[]> rows = List.of(
                new Object[] { 1, "12A", 3, "Económica", 15000, "seleccionado" },
                new Object[] { 2, "12B", 3, "Económica", 15000, "disponible" },
                new Object[] { 3, "2A", 1, "Primera", 40000, "disponible" },
                new Object[] { 4, "15C", 4, "Básica", 8000, "disponible" }
        );
        when(reservas.mobileMapa(idVuelo, idReserva, rut)).thenReturn(rows);

        MapaAsientos mapa = operationsService.mapa(rut, idReserva, idVuelo);

        assertTrue(mapa.cambioPermitido());
        assertEquals(15000, mapa.precioActual());

        // 1. Asiento en misma clase (12B: $15.000)
        AsientoDto asientoMismaClase = mapa.asientos().stream().filter(a -> a.idAsiento() == 2).findFirst().orElseThrow();
        assertTrue(asientoMismaClase.seleccionable());
        assertFalse(asientoMismaClase.esUpgrade());
        assertEquals(0, asientoMismaClase.diferenciaPrecio());
        assertEquals(0, asientoMismaClase.cargoGestion());
        assertEquals(0, asientoMismaClase.totalPagar());

        // 2. Asiento Upgrade (2A: $40.000)
        // Diff = 40000 - 15000 = 25000
        // Fee = Math.round(25000 * 0.15) = 3750
        // Total = 28750
        AsientoDto asientoUpgrade = mapa.asientos().stream().filter(a -> a.idAsiento() == 3).findFirst().orElseThrow();
        assertTrue(asientoUpgrade.seleccionable());
        assertTrue(asientoUpgrade.esUpgrade());
        assertEquals(25000, asientoUpgrade.diferenciaPrecio());
        assertEquals(3750, asientoUpgrade.cargoGestion());
        assertEquals(28750, asientoUpgrade.totalPagar());

        // 3. Asiento Downgrade (15C: $8.000 < $15.000)
        AsientoDto asientoDowngrade = mapa.asientos().stream().filter(a -> a.idAsiento() == 4).findFirst().orElseThrow();
        assertFalse(asientoDowngrade.seleccionable(), "Downgrade no debe ser seleccionable en autoservicio");
    }

    @Test
    @DisplayName("Plazo de tarifa estricto: aun dentro de ventana de check-in o con check-in hecho, bloquea si faltan menos horas que las exigidas")
    void debeBloquearCambioSiNoCumpleHorasMinimasAunConCheckin() {
        String rut = "11111111-1";
        int idReserva = 10;
        int idVuelo = 55;
        // Vuelo en 30 horas: faltan menos de las 72h de la tarifa (incluso con check-in realizado)
        LocalDateTime salida = LocalDateTime.now(fixedClock).plusHours(30);

        detalleActual = crearDetalleReserva(salida, true);
        when(reservas.mobileMapa(idVuelo, idReserva, rut)).thenReturn(List.of());

        MapaAsientos mapa = operationsService.mapa(rut, idReserva, idVuelo);

        assertFalse(mapa.cambioPermitido(), "Debe bloquear si faltan 30h y la tarifa requiere 72h aun con check-in");
        assertNotNull(mapa.motivo());
        assertTrue(mapa.motivo().contains("plazo límite estipulado por tu tarifa"));
    }

    @Test
    @DisplayName("Fuera de ventana de check-in: si faltan 50h pero la tarifa exige 72h y no está en ventana, se evalúa la regla")
    void debeBloquearCambioFueraDeVentanaSiNoCumpleHorasMinimas() {
        String rut = "11111111-1";
        int idReserva = 10;
        int idVuelo = 55;
        // Vuelo en 50 horas (fuera de las 48h de check-in, pero la tarifa pide 72h)
        LocalDateTime salida = LocalDateTime.now(fixedClock).plusHours(50);

        detalleActual = crearDetalleReserva(salida, false);
        when(reservas.mobileMapa(idVuelo, idReserva, rut)).thenReturn(List.of());

        MapaAsientos mapa = operationsService.mapa(rut, idReserva, idVuelo);

        assertFalse(mapa.cambioPermitido(), "Debe bloquear si faltan 50h y la tarifa requiere 72h fuera de ventana check-in");
        assertNotNull(mapa.motivo());
        assertTrue(mapa.motivo().contains("plazo límite estipulado por tu tarifa"));
    }

    @Test
    @DisplayName("Check-in bloqueado si faltan más de 48 horas para la salida del vuelo")
    void debeBloquearCheckinAntesDe48Horas() {
        String rut = "11111111-1";
        int idReserva = 10;
        // Vuelo en 50 horas (> 48h)
        LocalDateTime salida = LocalDateTime.now(fixedClock).plusHours(50);
        detalleActual = crearDetalleReserva(salida, false);

        MobileException ex = assertThrows(MobileException.class, () -> operationsService.checkin(rut, idReserva));
        assertTrue(ex.getMessage().contains("48 horas antes"));
    }

    @Test
    @DisplayName("Check-in bloqueado si faltan menos de 60 minutos para la salida del vuelo")
    void debeBloquearCheckinDespuesDeCierre60Minutos() {
        String rut = "11111111-1";
        int idReserva = 10;
        // Vuelo en 30 minutos (< 60m)
        LocalDateTime salida = LocalDateTime.now(fixedClock).plusMinutes(30);
        detalleActual = crearDetalleReserva(salida, false);

        MobileException ex = assertThrows(MobileException.class, () -> operationsService.checkin(rut, idReserva));
        assertTrue(ex.getMessage().contains("cerró 60 minutos antes"));
    }

    @Test
    @DisplayName("Check-in exitoso dentro de la ventana (48h a 60 min)")
    void debePermitirCheckinEnVentana() {
        String rut = "11111111-1";
        int idReserva = 10;
        // Vuelo en 24 horas (dentro de ventana)
        LocalDateTime salida = LocalDateTime.now(fixedClock).plusHours(24);
        detalleActual = crearDetalleReserva(salida, false);

        when(reservas.mobileFaltanAsientos(eq(idReserva), any())).thenReturn(List.of());
        List<Object[]> checkinsMock = Collections.singletonList(new Object[]{99, idReserva, Timestamp.valueOf(LocalDateTime.now(fixedClock)), "App"});
        when(reservas.mobileCheckins(idReserva)).thenReturn(checkinsMock);

        CheckinDto resultado = operationsService.checkin(rut, idReserva);

        assertNotNull(resultado);
        assertEquals(99, resultado.idCheckin());
        verify(reservas).mobileInsertCheckin(eq(idReserva), any());
    }
}
