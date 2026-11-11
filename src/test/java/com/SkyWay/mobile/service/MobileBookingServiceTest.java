package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.PasajeroReserva.domain.repository.PasajeroReservaRepository;
import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import com.SkyWay.modules.estadoreserva.domain.repository.EstadoReservaRepository;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.repository.PasajeroRepository;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.reservaitinerario.domain.repository.ReservaItinerarioRepository;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.repository.ItinerarioTarifaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MobileBookingServiceTest {

    private MobileAuthService auth;
    private AeropuertoRepository aeropuertos;
    private ItinerarioService itinerarios;
    private ItinerarioTarifaRepository tarifas;
    private ReservaRepository reservas;
    private ReservaItinerarioRepository reservaItinerarios;
    private PasajeroRepository pasajeros;
    private PasajeroReservaRepository pasajerosReservas;
    private EstadoReservaRepository estados;
    private Clock fixedClock;
    private MobileBookingService bookingService;

    private final Instant nowInstant = Instant.parse("2026-11-09T12:00:00Z");
    private final ZoneId zone = ZoneId.of("UTC");

    @BeforeEach
    void setUp() {
        org.springframework.security.crypto.password.PasswordEncoder noopEncoder = new org.springframework.security.crypto.password.PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) { return String.valueOf(rawPassword); }
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) { return true; }
        };
        fixedClock = Clock.fixed(nowInstant, zone);
        auth = new MobileAuthService(null, null, noopEncoder, null, fixedClock, null, 7200) {
            @Override
            public void requirePassenger(String rut) {
            }
        };
        aeropuertos = mock(AeropuertoRepository.class);
        itinerarios = mock(ItinerarioService.class);
        tarifas = mock(ItinerarioTarifaRepository.class);
        reservas = mock(ReservaRepository.class);
        reservaItinerarios = mock(ReservaItinerarioRepository.class);
        pasajeros = mock(PasajeroRepository.class);
        pasajerosReservas = mock(PasajeroReservaRepository.class);
        estados = mock(EstadoReservaRepository.class);

        bookingService = new MobileBookingService(
                auth, aeropuertos, itinerarios, tarifas, reservas, reservaItinerarios,
                pasajeros, pasajerosReservas, estados, fixedClock, null, null, null
        );
    }

    @Test
    @DisplayName("catalogoEquipajes retorna los 3 tipos estándar para cabina y bodega")
    void testCatalogoEquipajes() {
        List<TipoEquipajeCatalogo> lista = bookingService.catalogoEquipajes("19876543-2");
        assertNotNull(lista);
        assertEquals(3, lista.size());

        TipoEquipajeCatalogo cabina = lista.stream().filter(e -> "CABINA".equals(e.tipo())).findFirst().orElseThrow();
        assertEquals(1, cabina.maximoPermitido());
        assertEquals(15000, cabina.precio());

        TipoEquipajeCatalogo b15 = lista.stream().filter(e -> "BODEGA_15KG".equals(e.tipo())).findFirst().orElseThrow();
        assertEquals(3, b15.maximoPermitido());
        assertEquals(22000, b15.precio());

        TipoEquipajeCatalogo b23 = lista.stream().filter(e -> "BODEGA_23KG".equals(e.tipo())).findFirst().orElseThrow();
        assertEquals(3, b23.maximoPermitido());
        assertEquals(30000, b23.precio());
    }

    @Test
    @DisplayName("Rechazar reserva si se excede el límite máximo de equipaje de cabina (más de 1)")
    void testExcesoEquipajeCabina() {
        String rut = "19876543-2";
        Pasajero pasajero = new Pasajero();
        pasajero.setRut(rut);
        when(pasajeros.findById(rut)).thenReturn(Optional.of(pasajero));

        Itinerario itinerario = new Itinerario();
        itinerario.setIdItinerario(10);
        itinerario.setHoraSalida(Timestamp.valueOf(LocalDateTime.now(fixedClock).plusDays(2)));
        when(itinerarios.findById(10)).thenReturn(itinerario);

        ItinerarioTarifa it = new ItinerarioTarifa();
        it.setIdItinerarioTarifa(100);
        it.setItinerario(itinerario);
        it.setPrecio(BigDecimal.valueOf(45000));
        when(tarifas.findById(100)).thenReturn(Optional.of(it));

        List<ItemEquipajeSolicitud> equipajes = List.of(
                new ItemEquipajeSolicitud(1, "CABINA", BigDecimal.valueOf(10), "40x30x20", 15000),
                new ItemEquipajeSolicitud(1, "CABINA", BigDecimal.valueOf(10), "40x30x20", 15000)
        );

        CrearReservaRequest req = new CrearReservaRequest(10, 100, 1, equipajes);

        MobileException ex = assertThrows(MobileException.class, () -> bookingService.crear(rut, req));
        assertEquals("LIMITE_EQUIPAJE", ex.getCodigo());
    }

    @Test
    @DisplayName("Rechazar reserva si se excede el límite de maletas de bodega (más de 3 en total)")
    void testExcesoEquipajeBodega() {
        String rut = "19876543-2";
        Pasajero pasajero = new Pasajero();
        pasajero.setRut(rut);
        when(pasajeros.findById(rut)).thenReturn(Optional.of(pasajero));

        Itinerario itinerario = new Itinerario();
        itinerario.setIdItinerario(10);
        itinerario.setHoraSalida(Timestamp.valueOf(LocalDateTime.now(fixedClock).plusDays(2)));
        when(itinerarios.findById(10)).thenReturn(itinerario);

        ItinerarioTarifa it = new ItinerarioTarifa();
        it.setIdItinerarioTarifa(100);
        it.setItinerario(itinerario);
        it.setPrecio(BigDecimal.valueOf(45000));
        when(tarifas.findById(100)).thenReturn(Optional.of(it));

        List<ItemEquipajeSolicitud> equipajes = List.of(
                new ItemEquipajeSolicitud(2, "BODEGA_15KG", BigDecimal.valueOf(15), "158cm", 22000),
                new ItemEquipajeSolicitud(2, "BODEGA_15KG", BigDecimal.valueOf(15), "158cm", 22000),
                new ItemEquipajeSolicitud(3, "BODEGA_23KG", BigDecimal.valueOf(23), "158cm", 30000),
                new ItemEquipajeSolicitud(3, "BODEGA_23KG", BigDecimal.valueOf(23), "158cm", 30000)
        );

        CrearReservaRequest req = new CrearReservaRequest(10, 100, 1, equipajes);

        MobileException ex = assertThrows(MobileException.class, () -> bookingService.crear(rut, req));
        assertEquals("LIMITE_EQUIPAJE", ex.getCodigo());
    }

    @Test
    @DisplayName("Crear reserva móvil exitosa con cálculo de total, PNR generado y estado Confirmada")
    void testCrearReservaExitosa() {
        String rut = "19876543-2";
        Pasajero pasajero = new Pasajero();
        pasajero.setRut(rut);
        when(pasajeros.findById(rut)).thenReturn(Optional.of(pasajero));

        Itinerario itinerario = new Itinerario();
        itinerario.setIdItinerario(10);
        itinerario.setHoraSalida(Timestamp.valueOf(LocalDateTime.now(fixedClock).plusDays(2)));
        when(itinerarios.findById(10)).thenReturn(itinerario);

        Tarifa tarifa = new Tarifa();
        tarifa.setNombre("Flexible");
        ItinerarioTarifa it = new ItinerarioTarifa();
        it.setIdItinerarioTarifa(100);
        it.setItinerario(itinerario);
        it.setTarifa(tarifa);
        it.setPrecio(BigDecimal.valueOf(45000));
        when(tarifas.findById(100)).thenReturn(Optional.of(it));

        EstadoReserva estado = new EstadoReserva();
        estado.setIdEstadoReserva(2);
        estado.setDescripcion("Confirmada");
        when(estados.findFirstByDescripcionIgnoreCase("Confirmada")).thenReturn(Optional.of(estado));

        Reserva rGuardada = new Reserva();
        rGuardada.setIdReserva(555);
        rGuardada.setTotal(BigDecimal.valueOf(67000)); // 45000 + 22000 bodega
        when(reservas.saveAndFlush(any())).thenReturn(rGuardada);

        List<ItemEquipajeSolicitud> equipajes = List.of(
                new ItemEquipajeSolicitud(2, "BODEGA_15KG", BigDecimal.valueOf(15), "158cm", 22000)
        );

        CrearReservaRequest req = new CrearReservaRequest(10, 100, 1, equipajes);
        ReservaCreada creada = bookingService.crear(rut, req);

        assertNotNull(creada);
        assertEquals(555, creada.idReserva());
        assertEquals("Confirmada", creada.estado());
        assertNotNull(creada.codigoPnr());
        assertEquals(6, creada.codigoPnr().length());
        assertEquals(BigDecimal.valueOf(67000), creada.total());
    }

    @Test
    @DisplayName("Rechazar reserva si se envía método de pago no permitido (ej. MercadoPago o id inválido)")
    void testMetodoPagoInvalido() {
        String rut = "19876543-2";
        Pasajero pasajero = new Pasajero();
        pasajero.setRut(rut);
        when(pasajeros.findById(rut)).thenReturn(Optional.of(pasajero));

        Itinerario itinerario = new Itinerario();
        itinerario.setIdItinerario(10);
        itinerario.setHoraSalida(Timestamp.valueOf(LocalDateTime.now(fixedClock).plusDays(2)));
        when(itinerarios.findById(10)).thenReturn(itinerario);

        Tarifa tarifa = new Tarifa();
        tarifa.setNombre("Flexible");
        ItinerarioTarifa it = new ItinerarioTarifa();
        it.setIdItinerarioTarifa(100);
        it.setItinerario(itinerario);
        it.setTarifa(tarifa);
        it.setPrecio(BigDecimal.valueOf(45000));
        when(tarifas.findById(100)).thenReturn(Optional.of(it));

        EstadoReserva estado = new EstadoReserva();
        estado.setIdEstadoReserva(2);
        estado.setDescripcion("Confirmada");
        when(estados.findFirstByDescripcionIgnoreCase("Confirmada")).thenReturn(Optional.of(estado));

        Reserva rGuardada = new Reserva();
        rGuardada.setIdReserva(555);
        rGuardada.setTotal(BigDecimal.valueOf(45000));
        when(reservas.saveAndFlush(any())).thenReturn(rGuardada);

        CrearReservaRequest req = new CrearReservaRequest(10, 100, 3, List.of()); // 3 = MercadoPago (no permitido)
        MobileException ex = assertThrows(MobileException.class, () -> bookingService.crear(rut, req));
        assertEquals("METODO_PAGO_INVALIDO", ex.getCodigo());
    }
}
