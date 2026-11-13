package com.SkyWay.mobile.service;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.pago.SolicitudPagoDTO;
import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.PasajeroReserva.domain.model.PasajeroReserva;
import com.SkyWay.modules.PasajeroReserva.domain.repository.PasajeroReservaRepository;
import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.estadoreserva.domain.repository.EstadoReservaRepository;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.metodopago.domain.service.MetodoPagoService;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.pago.domain.service.PagoService;
import com.SkyWay.modules.pasajero.domain.repository.PasajeroRepository;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.repository.ReservaItinerarioRepository;
import com.SkyWay.modules.tarifaItinerario.domain.repository.ItinerarioTarifaRepository;
import com.SkyWay.util.Logger;
import com.SkyWay.util.PnrGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final MetodoPagoService metodoPagoService;
    private final PagoService pagoService;
    private final EquipajeService equipajeService;

    @PersistenceContext
    private EntityManager em;

    @Autowired(required = false)
    private PagoFactoryService pagoFactoryService;

    @Autowired(required = false)
    private com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService asientoCacheService;

    private record SolicitudPagoPendienteMovil(String rut, CrearReservaRequest request, BigDecimal totalFinal) {}
    private final java.util.concurrent.ConcurrentHashMap<String, SolicitudPagoPendienteMovil> pagosPendientes =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
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
            Clock clock,
            @Autowired(required = false) MetodoPagoService metodoPagoService,
            @Autowired(required = false) PagoService pagoService,
            @Autowired(required = false) EquipajeService equipajeService) {
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
        this.metodoPagoService = metodoPagoService;
        this.pagoService = pagoService;
        this.equipajeService = equipajeService;
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
        if (fecha != null && fecha.isBefore(LocalDate.now(clock))) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "FECHA_INVALIDA", "La fecha de viaje no puede estar en el pasado.");
        }

        try {
            String fechaStr = fecha != null ? fecha.toString() : null;
            LocalDateTime ahora = LocalDateTime.now(clock);
            var dtos = itinerarios.buscarItinerarios(from, to, fechaStr);
            if (dtos.isEmpty()) return List.of();
            List<Integer> ids = dtos.stream().map(com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO::getItinerario).toList();
            var tarifasMap = tarifas.findByItinerarioIdItinerarioIn(ids).stream()
                    .filter(t -> t.getItinerario() != null && t.getTarifa() != null)
                    .collect(java.util.stream.Collectors.groupingBy(t -> t.getItinerario().getIdItinerario()));

            return dtos.stream()
                    .map(dto -> {
                        var entity = itinerarios.findById(dto.getItinerario());
                        if (entity == null || entity.getHoraSalida() == null) return null;
                        LocalDateTime salida = entity.getHoraSalida().toLocalDateTime();
                        if (!salida.isAfter(ahora)) return null;
                        LocalDateTime llegada = entity.getHoraLlegada() == null ? null : entity.getHoraLlegada().toLocalDateTime();
                        var opciones = tarifasMap.getOrDefault(entity.getIdItinerario(), List.of()).stream()
                                .map(t -> new TarifaDisponible(
                                        t.getIdItinerarioTarifa(),
                                        t.getTarifa().getIdTarifa(),
                                        t.getTarifa().getNombre(),
                                        t.getPrecio()))
                                .sorted(Comparator.comparing(TarifaDisponible::precio))
                                .toList();
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

    public List<DiaTarifaFlexible> calendarioFlexible(String rut, String origen, String destino, LocalDate fechaCentro) {
        auth.requirePassenger(rut);
        String from = normalizarIata(origen, "origen");
        String to = normalizarIata(destino, "destino");
        if (from.equals(to)) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "RUTA_INVALIDA", "El origen y el destino deben ser distintos.");
        }
        LocalDate hoy = LocalDate.now(clock);
        LocalDate base = (fechaCentro != null && !fechaCentro.isBefore(hoy)) ? fechaCentro : hoy;
        List<DiaTarifaFlexible> dias = new java.util.ArrayList<>();
        for (int offset = -3; offset <= 3; offset++) {
            LocalDate dia = base.plusDays(offset);
            if (dia.isBefore(hoy)) continue;
            try {
                var disponibles = buscar(rut, from, to, dia);
                BigDecimal minPrecio = null;
                for (var it : disponibles) {
                    for (var t : it.tarifas()) {
                        if (t.precio() != null && (minPrecio == null || t.precio().compareTo(minPrecio) < 0)) {
                            minPrecio = t.precio();
                        }
                    }
                }
                dias.add(new DiaTarifaFlexible(dia, disponibles.size(), minPrecio));
            } catch (Exception ex) {
                dias.add(new DiaTarifaFlexible(dia, 0, null));
            }
        }
        return dias;
    }

    public List<TipoEquipajeCatalogo> catalogoEquipajes(String rut) {
        auth.requirePassenger(rut);
        return List.of(
                new TipoEquipajeCatalogo(1, "CABINA", "Equipaje de Cabina (10 kg)", "Bolso o maleta carry-on para compartimento superior", 15000, 10, "40x30x20", 1),
                new TipoEquipajeCatalogo(2, "BODEGA_15KG", "Maleta de Bodega (15 kg)", "Maleta facturada estándar para bodega", 22000, 15, "158 cm", 3),
                new TipoEquipajeCatalogo(3, "BODEGA_23KG", "Maleta de Bodega (23 kg)", "Maleta facturada extra grande para bodega", 30000, 23, "158 cm", 3)
        );
    }

    public List<VueloConAsientos> vuelosConAsientos(String rut, int idItinerario) {
        auth.requirePassenger(rut);
        var itinerario = itinerarios.findById(idItinerario);
        if (itinerario == null) {
            throw MobileException.notFound();
        }

        if (em == null) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<Object[]> vuelosItin = em.createNativeQuery("""
            SELECT iv.id_vuelo, iv.orden, v.numero_vuelo, v.fecha_hora_salida, v.fecha_hora_llegada,
                   m.nombre AS nombre_modelo
            FROM itinerario_vuelo iv
            JOIN vuelo v ON v.id_vuelo = iv.id_vuelo
            JOIN avion av ON av.id_avion = v.id_avion
            JOIN modelo_avion m ON m.id_modelo = av.id_modelo
            WHERE iv.id_itinerario = :idItin
            ORDER BY iv.orden ASC
        """)
        .setParameter("idItin", idItinerario)
        .getResultList();

        List<VueloConAsientos> resultado = new java.util.ArrayList<>();

        for (Object[] vRow : vuelosItin) {
            int idVuelo = ((Number) vRow[0]).intValue();
            String numVuelo = vRow[2] != null ? vRow[2].toString() : null;
            LocalDateTime fSalida = vRow[3] instanceof Timestamp ts ? ts.toLocalDateTime() : null;
            LocalDateTime fLlegada = vRow[4] instanceof Timestamp ts ? ts.toLocalDateTime() : null;
            String nombreModeloAvion = vRow[5] != null ? vRow[5].toString() : "Aeronave Comercial";

            // Origen y Destino del vuelo
            String origen = "";
            String destino = "";
            try {
                @SuppressWarnings("unchecked")
                List<Object[]> origDest = em.createNativeQuery("""
                    SELECT
                        (SELECT ao.codigo_iata FROM segmento_vuelo sv JOIN aeropuerto ao ON ao.id_aeropuerto = sv.id_aeropuerto_origen WHERE sv.id_vuelo = :vId ORDER BY sv.orden_segmento ASC LIMIT 1) AS origen,
                        (SELECT ad.codigo_iata FROM segmento_vuelo sv JOIN aeropuerto ad ON ad.id_aeropuerto = sv.id_aeropuerto_destino WHERE sv.id_vuelo = :vId ORDER BY sv.orden_segmento DESC LIMIT 1) AS destino
                """).setParameter("vId", idVuelo).getResultList();
                if (!origDest.isEmpty()) {
                    Object[] od = origDest.get(0);
                    if (od[0] != null) origen = od[0].toString();
                    if (od[1] != null) destino = od[1].toString();
                }
            } catch (Exception ignored) {}

            // Configuraciones de cabina
            List<ConfiguracionCabinaItemDto> cabinas = new java.util.ArrayList<>();
            boolean avionDoblePasillo = false;
            try {
                @SuppressWarnings("unchecked")
                List<Object[]> cfgRows = em.createNativeQuery("""
                    SELECT cc.id_configuracion, ca.descripcion, cc.fila_inicio, cc.fila_fin,
                           cc.distribucion_columnas, cc.letras_columnas, COALESCE(cc.es_salida_emergencia, FALSE)
                    FROM vuelo v
                    JOIN avion av ON av.id_avion = v.id_avion
                    JOIN modelo_avion m ON m.id_modelo = av.id_modelo
                    LEFT JOIN configuracion_cabina cc ON cc.id_modelo = m.id_modelo
                    LEFT JOIN clase_asiento ca ON ca.id_clase = cc.id_clase
                    WHERE v.id_vuelo = :vId
                    ORDER BY cc.fila_inicio ASC
                """).setParameter("vId", idVuelo).getResultList();

                for (Object[] cr : cfgRows) {
                    if (cr[0] != null) {
                        Integer idCfg = ((Number) cr[0]).intValue();
                        String claseCfg = cr[1] != null ? cr[1].toString() : "Económica";
                        Integer fIni = cr[2] != null ? ((Number) cr[2]).intValue() : 1;
                        Integer fFin = cr[3] != null ? ((Number) cr[3]).intValue() : 30;
                        String dist = cr[4] != null ? cr[4].toString() : "3-3";
                        String letras = cr[5] != null ? cr[5].toString() : "ABCDEF";
                        Boolean esEmerg = cr[6] != null ? (Boolean) cr[6] : false;
                        cabinas.add(new ConfiguracionCabinaItemDto(idCfg, claseCfg, fIni, fFin, dist, letras, esEmerg));
                    }
                }

                avionDoblePasillo = cabinas.stream()
                        .anyMatch(cc -> cc.distribucionColumnas() != null &&
                                cc.distribucionColumnas().chars().filter(ch -> ch == '-').count() >= 2);
            } catch (Exception ignored) {}

            // Asientos del avión para este vuelo
            @SuppressWarnings("unchecked")
            List<Object[]> seatRows = em.createNativeQuery("""
                SELECT a.id_asiento, a.numero_asiento, a.id_clase, c.descripcion, pa.precio,
                       CASE 
                           WHEN ra.id_reserva_asiento IS NOT NULL THEN 'ocupado'
                           ELSE 'disponible'
                       END AS estado,
                       a.fila, a.letra,
                       COALESCE(a.es_pasillo, FALSE) AS es_pasillo,
                       COALESCE(a.es_emergencia, FALSE) AS es_emergencia
                FROM vuelo v
                JOIN asiento a ON a.id_avion = v.id_avion
                LEFT JOIN clase_asiento c ON c.id_clase = a.id_clase
                LEFT JOIN precio_asiento pa ON pa.id_vuelo = v.id_vuelo AND pa.id_clase = a.id_clase
                LEFT JOIN reserva_asiento ra ON ra.id_vuelo = v.id_vuelo AND ra.id_asiento = a.id_asiento
                WHERE v.id_vuelo = :vId
                ORDER BY COALESCE(a.fila, NULLIF(regexp_replace(a.numero_asiento, '[^0-9]', '', 'g'), '')::int, 0) ASC,
                         COALESCE(NULLIF(a.letra, ''), regexp_replace(a.numero_asiento, '[0-9]', '', 'g')) ASC
            """).setParameter("vId", idVuelo).getResultList();

            List<AsientoVueloDisponible> asientos = new java.util.ArrayList<>();
            for (Object[] r : seatRows) {
                Integer idAsiento = ((Number) r[0]).intValue();
                String numSeat = r[1] != null ? r[1].toString() : "";
                Integer idClase = r[2] != null ? ((Number) r[2]).intValue() : 1;
                String clase = r[3] != null ? r[3].toString() : "Económica";
                Integer precio = r[4] != null ? ((Number) r[4]).intValue() : (idClase == 3 ? 30000 : (idClase == 2 ? 15000 : 0));
                String estado = r[5] != null ? r[5].toString() : "disponible";

                Integer fila = r[6] != null ? ((Number) r[6]).intValue() : null;
                String letra = r[7] != null ? r[7].toString() : null;
                Boolean esPasillo = r[8] != null ? (Boolean) r[8] : false;
                Boolean esEmergencia = r[9] != null ? (Boolean) r[9] : false;

                if (fila == null) {
                    String d = numSeat.replaceAll("[^0-9]", "");
                    fila = d.isEmpty() ? 1 : Integer.parseInt(d);
                }
                if (letra == null || letra.isBlank()) {
                    String l = numSeat.replaceAll("[0-9]", "").trim();
                    letra = l.isEmpty() ? numSeat : l;
                }

                asientos.add(new AsientoVueloDisponible(idAsiento, numSeat, fila, letra, idClase, clase, precio, estado, esPasillo, esEmergencia));
            }

            resultado.add(new VueloConAsientos(
                    idVuelo, numVuelo, origen, destino, fSalida, fLlegada,
                    nombreModeloAvion, avionDoblePasillo, cabinas, asientos
            ));
        }

        return resultado;
    }

    public IniciarPagoReservaResponse iniciarPago(String rut, IniciarPagoReservaRequest request) {
        auth.requirePassenger(rut);
        pasajeros.findById(rut).orElseThrow(MobileException::notFound);
        var itinerario = itinerarios.findById(request.idItinerario());
        if (itinerario == null) throw MobileException.notFound();

        var tarifa = tarifas.findById(request.idItinerarioTarifa()).orElseThrow(MobileException::notFound);
        if (tarifa.getItinerario() == null
                || !request.idItinerario().equals(tarifa.getItinerario().getIdItinerario())) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "TARIFA_INVALIDA", "La tarifa no pertenece al itinerario seleccionado.");
        }

        BigDecimal tarifaRegresoPrecio = BigDecimal.ZERO;
        if (request.idItinerarioRegreso() != null) {
            if (request.idItinerarioTarifaRegreso() == null) {
                throw new MobileException(HttpStatus.BAD_REQUEST, "TARIFA_REGRESO_REQUERIDA", "Selecciona una tarifa para el vuelo de regreso.");
            }
            var itinReg = itinerarios.findById(request.idItinerarioRegreso());
            if (itinReg == null) throw MobileException.notFound();
            var tarReg = tarifas.findById(request.idItinerarioTarifaRegreso()).orElseThrow(MobileException::notFound);
            if (tarReg.getItinerario() == null || !request.idItinerarioRegreso().equals(tarReg.getItinerario().getIdItinerario())) {
                throw new MobileException(HttpStatus.BAD_REQUEST, "TARIFA_INVALIDA", "La tarifa de regreso no pertenece al itinerario de regreso.");
            }
            tarifaRegresoPrecio = tarReg.getPrecio() != null ? tarReg.getPrecio() : BigDecimal.ZERO;
        }

        List<AcompananteSolicitud> acompanantes = normalizarAcompanantes(rut, request.acompanantes());
        int totalPasajeros = 1 + acompanantes.size();

        int idMetodo = request.idMetodoPago() != null ? request.idMetodoPago() : 1;
        if (idMetodo != 1 && idMetodo != 2) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "METODO_PAGO_INVALIDO",
                    "Método de pago no soportado. Seleccione Webpay Plus (1) o PayPal (2).");
        }

        BigDecimal totalEquipaje = BigDecimal.ZERO;
        List<ItemEquipajeSolicitud> itemsEquipaje = request.equipaje() != null ? request.equipaje() : List.of();
        long cantCabina = 0;
        long cantBodega = 0;
        for (var item : itemsEquipaje) {
            String tipo = item.tipo() != null ? item.tipo().toUpperCase() : "";
            if (tipo.contains("CABINA") || Integer.valueOf(1).equals(item.idTipoEquipaje())) {
                cantCabina++;
            } else {
                cantBodega++;
            }
            totalEquipaje = totalEquipaje.add(BigDecimal.valueOf(precioEquipajeCatalogo(item)));
        }

        if (cantCabina > totalPasajeros) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "LIMITE_EQUIPAJE",
                    "Máximo 1 equipaje de cabina por pasajero.");
        }
        if (cantBodega > 3L * totalPasajeros) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "LIMITE_EQUIPAJE",
                    "Máximo 3 maletas de bodega por pasajero.");
        }

        java.util.Set<Integer> vuelosValidos = vuelosPermitidos(request.idItinerario(), request.idItinerarioRegreso());
        java.util.Set<String> asientosUnicosPorVuelo = new java.util.HashSet<>();
        BigDecimal totalAsientos = BigDecimal.ZERO;
        if (request.asientos() != null && !request.asientos().isEmpty()) {
            for (var sel : request.asientos()) {
                if (sel.idVuelo() != null && sel.idAsiento() != null && em != null) {
                    if (!vuelosValidos.isEmpty() && !vuelosValidos.contains(sel.idVuelo())) {
                        throw new MobileException(HttpStatus.BAD_REQUEST, "VUELO_INVALIDO",
                                "Uno de los vuelos seleccionados no pertenece al itinerario.");
                    }
                    if (!asientosUnicosPorVuelo.add(sel.idVuelo() + "::" + sel.idAsiento())) {
                        throw new MobileException(HttpStatus.BAD_REQUEST, "ASIENTO_DUPLICADO",
                                "No puedes asignar el mismo asiento a dos pasajeros en el mismo vuelo.");
                    }
                    List<?> rows = em.createNativeQuery("""
                        SELECT a.id_asiento, a.id_clase, COALESCE(pa.precio, 0)
                        FROM vuelo v
                        JOIN asiento a ON a.id_avion = v.id_avion
                        LEFT JOIN precio_asiento pa ON pa.id_vuelo = v.id_vuelo AND pa.id_clase = a.id_clase
                        WHERE v.id_vuelo = :idVuelo AND a.id_asiento = :idAsiento
                          AND NOT EXISTS (
                              SELECT 1 FROM reserva_asiento ra WHERE ra.id_vuelo = v.id_vuelo AND ra.id_asiento = a.id_asiento
                          )
                    """)
                    .setParameter("idVuelo", sel.idVuelo())
                    .setParameter("idAsiento", sel.idAsiento())
                    .getResultList();

                    if (rows.isEmpty()) {
                        throw new MobileException(HttpStatus.CONFLICT, "ASIENTO_OCUPADO",
                                "El asiento seleccionado ya no se encuentra disponible.");
                    }
                    Object[] r = (Object[]) rows.get(0);
                    Number pNum = (Number) r[2];
                    int p = pNum != null ? pNum.intValue() : 0;
                    totalAsientos = totalAsientos.add(BigDecimal.valueOf(p));
                }
            }
        }

        BigDecimal totalIda = tarifa.getPrecio() != null ? tarifa.getPrecio() : BigDecimal.ZERO;
        BigDecimal totalBase = totalIda.add(tarifaRegresoPrecio).multiply(BigDecimal.valueOf(totalPasajeros));
        BigDecimal totalFinal = totalBase.add(totalEquipaje).add(totalAsientos);

        String ordenCompra = "MOB-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        String base = (request.urlRetornoBase() != null && !request.urlRetornoBase().isBlank())
                ? request.urlRetornoBase().trim().replaceAll("/+$", "")
                : "http://localhost:8101";

        retenerAsientosEnCache(request.asientos(), ordenCompra);

        SolicitudPagoDTO solicitud = new SolicitudPagoDTO();
        solicitud.setOrdenCompra(ordenCompra);
        solicitud.setSessionId(ordenCompra);
        solicitud.setDescripcion("Reserva de vuelo SkyWay");

        MetodoPagoEnum metodoEnum = (idMetodo == 2) ? MetodoPagoEnum.PAYPAL : MetodoPagoEnum.WEBPAY;
        String pasarelaCode = (idMetodo == 2) ? "paypal" : "webpay";

        if (metodoEnum == MetodoPagoEnum.PAYPAL) {
            solicitud.setMoneda("USD");
            solicitud.setMonto(totalFinal.divide(BigDecimal.valueOf(950), 2, java.math.RoundingMode.HALF_UP));
            solicitud.setReturnUrl(base + "/inicio?pasarela=paypal&orden=" + ordenCompra);
            solicitud.setCancelUrl(base + "/inicio?pasarela=paypal&orden=" + ordenCompra + "&cancelado=true");
        } else {
            solicitud.setMoneda("CLP");
            solicitud.setMonto(totalFinal);
            solicitud.setReturnUrl(base + "/inicio?pasarela=webpay&orden=" + ordenCompra);
            solicitud.setCancelUrl(base + "/inicio?pasarela=webpay&orden=" + ordenCompra + "&cancelado=true");
        }

        if (pagoFactoryService == null) {
            liberarAsientosEnCache(request.asientos(), ordenCompra);
            throw new MobileException(HttpStatus.INTERNAL_SERVER_ERROR, "PASARELA_NO_DISPONIBLE",
                    "El servicio de pasarela de pago no está disponible.");
        }

        try {
            PasarelaPagoStrategy estrategia = pagoFactoryService.obtenerEstrategia(metodoEnum);
            String redirectUrl = estrategia.iniciarTransaccion(solicitud);
            CrearReservaRequest crearReq = new CrearReservaRequest(
                    request.idItinerario(),
                    request.idItinerarioTarifa(),
                    idMetodo,
                    itemsEquipaje,
                    request.asientos() != null ? request.asientos() : List.of(),
                    request.idItinerarioRegreso(),
                    request.idItinerarioTarifaRegreso(),
                    acompanantes
            );
            pagosPendientes.put(ordenCompra, new SolicitudPagoPendienteMovil(rut, crearReq, totalFinal));
            return new IniciarPagoReservaResponse(ordenCompra, pasarelaCode, redirectUrl);
        } catch (Exception ex) {
            liberarAsientosEnCache(request.asientos(), ordenCompra);
            Logger.logError("Error iniciando pago móvil en pasarela " + metodoEnum + ": " + ex.getMessage());
            throw new MobileException(HttpStatus.BAD_GATEWAY, "ERROR_PASARELA",
                    "No fue posible conectar con " + (idMetodo == 2 ? "PayPal" : "Webpay Plus") + ". Intenta nuevamente.");
        }
    }

    @Transactional
    public ReservaCreada confirmarPago(String rut, ConfirmarPagoReservaRequest request) {
        auth.requirePassenger(rut);
        SolicitudPagoPendienteMovil pendiente = pagosPendientes.remove(request.ordenCompra());
        if (pendiente == null || !normalizarRut(pendiente.rut()).equals(normalizarRut(rut))) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "PAGO_NO_ENCONTRADO",
                    "No se encontró una orden de pago válida para confirmar esta reserva.");
        }

        MetodoPagoEnum metodoEnum = ("paypal".equalsIgnoreCase(request.pasarela()) || "2".equals(request.pasarela()))
                ? MetodoPagoEnum.PAYPAL
                : MetodoPagoEnum.WEBPAY;

        if (pagoFactoryService == null) {
            liberarAsientosEnCache(pendiente.request().asientos(), request.ordenCompra());
            throw new MobileException(HttpStatus.INTERNAL_SERVER_ERROR, "PASARELA_NO_DISPONIBLE",
                    "El servicio de pasarela de pago no está disponible.");
        }

        PasarelaPagoStrategy estrategia = pagoFactoryService.obtenerEstrategia(metodoEnum);
        boolean autorizado = estrategia.confirmarPago(request.token());
        if (!autorizado) {
            liberarAsientosEnCache(pendiente.request().asientos(), request.ordenCompra());
            throw new MobileException(HttpStatus.BAD_REQUEST, "PAGO_RECHAZADO",
                    "La transacción no fue autorizada o fue rechazada por " + (metodoEnum == MetodoPagoEnum.PAYPAL ? "PayPal" : "Webpay Plus") + ".");
        }

        ReservaCreada creada = crear(rut, pendiente.request());
        confirmarAsientosEnCache(pendiente.request().asientos());
        return creada;
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

        com.SkyWay.modules.itinerario.domain.model.Itinerario itinerarioRegreso = null;
        com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa tarifaRegreso = null;
        BigDecimal precioRegreso = BigDecimal.ZERO;
        if (request.idItinerarioRegreso() != null) {
            if (request.idItinerarioTarifaRegreso() == null) {
                throw new MobileException(HttpStatus.BAD_REQUEST, "TARIFA_REGRESO_REQUERIDA", "Selecciona una tarifa para el vuelo de regreso.");
            }
            itinerarioRegreso = itinerarios.findById(request.idItinerarioRegreso());
            if (itinerarioRegreso == null) throw MobileException.notFound();
            tarifaRegreso = tarifas.findById(request.idItinerarioTarifaRegreso()).orElseThrow(MobileException::notFound);
            if (tarifaRegreso.getItinerario() == null || !request.idItinerarioRegreso().equals(tarifaRegreso.getItinerario().getIdItinerario())) {
                throw new MobileException(HttpStatus.BAD_REQUEST, "TARIFA_INVALIDA", "La tarifa de regreso no pertenece al itinerario de regreso.");
            }
            precioRegreso = tarifaRegreso.getPrecio() != null ? tarifaRegreso.getPrecio() : BigDecimal.ZERO;
        }

        List<AcompananteSolicitud> acompanantes = normalizarAcompanantes(rut, request.acompanantes());
        int totalPasajeros = 1 + acompanantes.size();

        // 2. Validación de límites y cálculo de equipaje adicional
        BigDecimal totalEquipaje = BigDecimal.ZERO;
        List<ItemEquipajeSolicitud> itemsEquipaje = request.equipaje() != null ? request.equipaje() : List.of();
        long cantCabina = 0;
        long cantBodega = 0;

        for (var item : itemsEquipaje) {
            String tipo = item.tipo() != null ? item.tipo().toUpperCase() : "";
            if (tipo.contains("CABINA") || Integer.valueOf(1).equals(item.idTipoEquipaje())) {
                cantCabina++;
            } else {
                cantBodega++;
            }
            totalEquipaje = totalEquipaje.add(BigDecimal.valueOf(precioEquipajeCatalogo(item)));
        }

        if (cantCabina > totalPasajeros) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "LIMITE_EQUIPAJE",
                    "Máximo 1 equipaje de cabina por pasajero.");
        }
        if (cantBodega > 3L * totalPasajeros) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "LIMITE_EQUIPAJE",
                    "Máximo 3 maletas de bodega por pasajero.");
        }

        // 3. Validación y cálculo de asientos seleccionados (soporta múltiples pasajeros y regreso)
        java.util.Set<Integer> vuelosValidos = vuelosPermitidos(request.idItinerario(), request.idItinerarioRegreso());
        java.util.Set<String> asientosUnicosPorVuelo = new java.util.HashSet<>();
        BigDecimal totalAsientos = BigDecimal.ZERO;
        java.util.Map<String, Integer> asientosSeleccionados = new java.util.HashMap<>();
        if (request.asientos() != null && !request.asientos().isEmpty()) {
            for (var sel : request.asientos()) {
                if (sel.idVuelo() != null && sel.idAsiento() != null) {
                    if (!vuelosValidos.isEmpty() && !vuelosValidos.contains(sel.idVuelo())) {
                        throw new MobileException(HttpStatus.BAD_REQUEST, "VUELO_INVALIDO",
                                "Uno de los vuelos seleccionados no pertenece al itinerario.");
                    }
                    if (!asientosUnicosPorVuelo.add(sel.idVuelo() + "::" + sel.idAsiento())) {
                        throw new MobileException(HttpStatus.BAD_REQUEST, "ASIENTO_DUPLICADO",
                                "No puedes asignar el mismo asiento a dos pasajeros en el mismo vuelo.");
                    }
                    String paxRut = (sel.rutPasajero() != null && !sel.rutPasajero().isBlank())
                            ? normalizarRut(sel.rutPasajero())
                            : normalizarRut(rut);
                    if (em != null) {
                        List<?> rows = em.createNativeQuery("""
                            SELECT a.id_asiento, a.id_clase, COALESCE(pa.precio, 0)
                            FROM vuelo v
                            JOIN asiento a ON a.id_avion = v.id_avion
                            LEFT JOIN precio_asiento pa ON pa.id_vuelo = v.id_vuelo AND pa.id_clase = a.id_clase
                            WHERE v.id_vuelo = :idVuelo AND a.id_asiento = :idAsiento
                              AND NOT EXISTS (
                                  SELECT 1 FROM reserva_asiento ra WHERE ra.id_vuelo = v.id_vuelo AND ra.id_asiento = a.id_asiento
                              )
                        """)
                        .setParameter("idVuelo", sel.idVuelo())
                        .setParameter("idAsiento", sel.idAsiento())
                        .getResultList();

                        if (rows.isEmpty()) {
                            throw new MobileException(HttpStatus.CONFLICT, "ASIENTO_OCUPADO",
                                    "El asiento seleccionado ya no se encuentra disponible.");
                        }
                        Object[] r = (Object[]) rows.get(0);
                        Number pNum = (Number) r[2];
                        int p = pNum != null ? pNum.intValue() : 0;
                        totalAsientos = totalAsientos.add(BigDecimal.valueOf(p));
                    }
                    asientosSeleccionados.put(sel.idVuelo() + "::" + paxRut, sel.idAsiento());
                }
            }
        }

        BigDecimal totalBase = (tarifa.getPrecio() != null ? tarifa.getPrecio() : BigDecimal.ZERO)
                .add(precioRegreso)
                .multiply(BigDecimal.valueOf(totalPasajeros));
        BigDecimal totalFinal = totalBase.add(totalEquipaje).add(totalAsientos);

        // 4. Estado Confirmada (ID: 2)
        var estado = estados.findFirstByDescripcionIgnoreCase("Confirmada")
                .orElseThrow(() -> new MobileException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "ESTADO_RESERVA_NO_CONFIGURADO",
                        "No existe el estado Confirmada en la base de datos."));

        // 5. Crear Encabezado de Reserva
        var reserva = new Reserva();
        reserva.setFechaReserva(Timestamp.valueOf(LocalDateTime.now(clock)));
        reserva.setEstadoReservaBean(estado);
        reserva.setPasajero(pasajero);
        reserva.setTotal(totalFinal);
        reserva = reservas.saveAndFlush(reserva);

        // 6. Generar PNR único oficial
        String pnr;
        if (em != null) {
            do {
                pnr = PnrGenerator.generarPnr();
            } while (!em.createQuery("SELECT r FROM Reserva r WHERE r.codigoReserva = :pnr", Reserva.class)
                    .setParameter("pnr", pnr)
                    .getResultList().isEmpty());
        } else {
            pnr = PnrGenerator.generarPnr();
        }
        reserva.setCodigoReserva(pnr);
        reserva = reservas.saveAndFlush(reserva);

        // 7. Asociar Itinerario(s) y Pasajero(s)
        var ri = new ReservaItinerario();
        ri.setReserva(reserva);
        ri.setItinerario(itinerario);
        ri.setItinerarioTarifa(tarifa);
        reservaItinerarios.saveAndFlush(ri);

        if (itinerarioRegreso != null && tarifaRegreso != null) {
            var riReg = new ReservaItinerario();
            riReg.setReserva(reserva);
            riReg.setItinerario(itinerarioRegreso);
            riReg.setItinerarioTarifa(tarifaRegreso);
            reservaItinerarios.saveAndFlush(riReg);
        }

        List<String> rutsReserva = new java.util.ArrayList<>();
        rutsReserva.add(rut);
        if (pasajerosReservas.findByReservaAndPasajero(reserva, pasajero).isEmpty()) {
            var pr = new PasajeroReserva();
            pr.setReserva(reserva);
            pr.setPasajero(pasajero);
            pasajerosReservas.saveAndFlush(pr);
        }
        for (var acomp : acompanantes) {
            var acompPax = asegurarAcompanante(acomp);
            String acompRut = acompPax.getRut();
            if (!rutsReserva.contains(acompRut)) {
                rutsReserva.add(acompRut);
            }
            if (pasajerosReservas.findByReservaAndPasajero(reserva, acompPax).isEmpty()) {
                var prAcomp = new PasajeroReserva();
                prAcomp.setReserva(reserva);
                prAcomp.setPasajero(acompPax);
                pasajerosReservas.saveAndFlush(prAcomp);
            }
        }

        // 8. Asignación de asientos seleccionados o fallback automático por vuelo y pasajero
        if (em != null) {
            asignarAsientosItinerario(reserva.getIdReserva(), itinerario.getIdItinerario(), tarifa, rutsReserva, asientosSeleccionados);
            if (itinerarioRegreso != null && tarifaRegreso != null) {
                asignarAsientosItinerario(reserva.getIdReserva(), itinerarioRegreso.getIdItinerario(), tarifaRegreso, rutsReserva, asientosSeleccionados);
            }
        }

        // 9. Registro de Equipaje en BD (asigna al pasajero indicado si forma parte de la reserva)
        if (em != null && !itemsEquipaje.isEmpty()) {
            java.util.Set<String> rutsNorm = rutsReserva.stream().map(MobileBookingService::normalizarRut).collect(java.util.stream.Collectors.toSet());
            for (var item : itemsEquipaje) {
                BigDecimal peso = item.peso() != null ? item.peso() : BigDecimal.valueOf(15);
                String dimensiones = item.dimensiones() != null ? item.dimensiones() : "Estándar";
                String tipoStr = item.tipo() != null ? item.tipo() : "BODEGA";
                int idTipo = item.idTipoEquipaje() != null ? item.idTipoEquipaje() :
                        (tipoStr.toUpperCase().contains("CABINA") ? 1 : (tipoStr.contains("23") ? 3 : 2));
                if (idTipo < 1 || idTipo > 3) {
                    idTipo = 2;
                }
                String rutBag = rut;
                if (item.rutPasajero() != null && !item.rutPasajero().isBlank()) {
                    String candNorm = normalizarRut(item.rutPasajero());
                    if (rutsNorm.contains(candNorm)) {
                        rutBag = rutsReserva.stream()
                                .filter(r -> normalizarRut(r).equals(candNorm))
                                .findFirst().orElse(rut);
                    }
                }
                em.createNativeQuery(
                    "INSERT INTO equipaje (peso, dimensiones, tipo, id_reserva, rut_pasajero, id_tipo) " +
                    "VALUES (:peso, :dimensiones, :tipo, :idReserva, :rut, :idTipo)"
                )
                .setParameter("peso", peso)
                .setParameter("dimensiones", dimensiones)
                .setParameter("tipo", tipoStr)
                .setParameter("idReserva", reserva.getIdReserva())
                .setParameter("rut", rutBag)
                .setParameter("idTipo", idTipo)
                .executeUpdate();
            }
        } else if (equipajeService != null && !itemsEquipaje.isEmpty()) {
            for (var item : itemsEquipaje) {
                Equipaje equipaje = new Equipaje();
                equipaje.setPeso(item.peso() != null ? item.peso() : BigDecimal.valueOf(15));
                equipaje.setDimensiones(item.dimensiones() != null ? item.dimensiones() : "Estándar");
                equipaje.setTipo(item.tipo() != null ? item.tipo() : "BODEGA");
                int idTipo = item.idTipoEquipaje() != null ? item.idTipoEquipaje() :
                        (equipaje.getTipo().contains("CABINA") ? 1 : 2);
                try {
                    equipajeService.guardar(equipaje, reserva.getIdReserva(), rut, idTipo);
                } catch (Exception ex) {
                    Logger.logWarn("Error al registrar equipaje móvil: " + ex.getMessage());
                }
            }
        }

        // 10. Validación y Registro del Pago en BD (Sólo WebPay Plus o PayPal)
        int idMetodo = request.idMetodoPago() != null ? request.idMetodoPago() : 1;
        if (idMetodo != 1 && idMetodo != 2) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "METODO_PAGO_INVALIDO",
                    "Método de pago no soportado. Seleccione Webpay Plus (1) o PayPal (2).");
        }
        String descMetodo = idMetodo == 2 ? "PayPal" : "Webpay Plus";
        if (em != null) {
            em.createNativeQuery(
                "INSERT INTO pago (monto_pagado, fecha, id_reserva, id_metodo_pago) " +
                "VALUES (:monto, NOW(), :idReserva, :idMetodo)"
            )
            .setParameter("monto", totalFinal)
            .setParameter("idReserva", reserva.getIdReserva())
            .setParameter("idMetodo", idMetodo)
            .executeUpdate();
        } else if (pagoService != null) {
            MetodoPago mp = new MetodoPago();
            mp.setIdMetodoPago(idMetodo);
            mp.setDescripcion(descMetodo);
            Pago pago = new Pago();
            pago.setMontoPagado(totalFinal);
            pago.setFecha(new Timestamp(System.currentTimeMillis()));
            pago.setReserva(reserva);
            pago.setMetodoPago(mp);
            pagoService.save(pago);
        }

        // 11. Auditoría en Log_Auditoria
        if (em != null) {
            try {
                em.createNativeQuery(
                    "INSERT INTO Log_Auditoria (rut_usuario, accion, nivel, tabla_afectada, id_registro_afectado, detalle, fecha_hora) " +
                    "VALUES (:rut, 'RESERVA_COMPLETADA_MOVIL', 'INFO'::tipo_nivel_log, 'Reserva', :idReserva, :detalle, NOW())"
                )
                .setParameter("rut", rut)
                .setParameter("idReserva", reserva.getIdReserva())
                .setParameter("detalle", "Reserva móvil #" + reserva.getIdReserva() + " (" + pnr + ") creada y pagada con éxito. Total: $" + totalFinal + ", Método: " + descMetodo)
                .executeUpdate();
            } catch (Exception logEx) {
                Logger.logWarn("Error al registrar Log_Auditoria en reserva móvil: " + logEx.getMessage());
            }
        }

        return new ReservaCreada(reserva.getIdReserva(), pnr, estado.getDescripcion(), totalFinal, descMetodo);
    }

    private void asignarAsientosItinerario(
            int idReserva,
            int idItinerario,
            com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa tarifa,
            List<String> rutsReserva,
            java.util.Map<String, Integer> asientosSeleccionados) {
        List<?> vuelosItinerario = em.createNativeQuery(
                "SELECT iv.id_vuelo FROM itinerario_vuelo iv WHERE iv.id_itinerario = :idItin ORDER BY iv.orden"
        )
        .setParameter("idItin", idItinerario)
        .getResultList();

        String nombreTarifa = tarifa.getTarifa() != null && tarifa.getTarifa().getNombre() != null
                ? tarifa.getTarifa().getNombre().toUpperCase() : "";
        int idClasePreferente = 1;
        if (nombreTarifa.contains("PREMIUM") || nombreTarifa.contains("PRIMERA")) {
            idClasePreferente = 3;
        } else if (nombreTarifa.contains("FLEXIBLE") || nombreTarifa.contains("EJECUTIVA")) {
            idClasePreferente = 2;
        }

        for (Object row : vuelosItinerario) {
            int idVuelo = ((Number) row).intValue();
            for (String paxRut : rutsReserva) {
                Integer selected = asientosSeleccionados.get(idVuelo + "::" + normalizarRut(paxRut));
                if (selected != null) {
                    em.createNativeQuery(
                        "INSERT INTO reserva_asiento (id_reserva, id_asiento, id_vuelo, rut) VALUES (:idReserva, :idAsiento, :idVuelo, :rut)"
                    )
                    .setParameter("idReserva", idReserva)
                    .setParameter("idAsiento", selected)
                    .setParameter("idVuelo", idVuelo)
                    .setParameter("rut", paxRut)
                    .executeUpdate();
                } else {
                    List<?> asientosLibres = em.createNativeQuery("""
                        SELECT a.id_asiento
                        FROM vuelo v
                        JOIN asiento a ON a.id_avion = v.id_avion
                        WHERE v.id_vuelo = :idVuelo
                          AND a.id_clase = :idClase
                          AND NOT EXISTS (
                              SELECT 1 FROM reserva_asiento ra WHERE ra.id_vuelo = v.id_vuelo AND ra.id_asiento = a.id_asiento
                          )
                        ORDER BY a.numero_asiento
                        LIMIT 1
                    """)
                    .setParameter("idVuelo", idVuelo)
                    .setParameter("idClase", idClasePreferente)
                    .getResultList();

                    if (asientosLibres.isEmpty()) {
                        asientosLibres = em.createNativeQuery("""
                            SELECT a.id_asiento
                            FROM vuelo v
                            JOIN asiento a ON a.id_avion = v.id_avion
                            WHERE v.id_vuelo = :idVuelo
                              AND NOT EXISTS (
                                  SELECT 1 FROM reserva_asiento ra WHERE ra.id_vuelo = v.id_vuelo AND ra.id_asiento = a.id_asiento
                              )
                            ORDER BY a.id_clase, a.numero_asiento
                            LIMIT 1
                        """)
                        .setParameter("idVuelo", idVuelo)
                        .getResultList();
                    }

                    if (!asientosLibres.isEmpty()) {
                        int idAsiento = ((Number) asientosLibres.get(0)).intValue();
                        em.createNativeQuery(
                            "INSERT INTO reserva_asiento (id_reserva, id_asiento, id_vuelo, rut) VALUES (:idReserva, :idAsiento, :idVuelo, :rut)"
                        )
                        .setParameter("idReserva", idReserva)
                        .setParameter("idAsiento", idAsiento)
                        .setParameter("idVuelo", idVuelo)
                        .setParameter("rut", paxRut)
                        .executeUpdate();
                    }
                }
            }
        }
    }

    private List<AcompananteSolicitud> normalizarAcompanantes(String titularRut, List<AcompananteSolicitud> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        if (raw.size() > 3) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "LIMITE_PASAJEROS", "Máximo 3 acompañantes por reserva móvil (4 pasajeros en total).");
        }
        String titularNorm = normalizarRut(titularRut);
        java.util.LinkedHashMap<String, AcompananteSolicitud> unicos = new java.util.LinkedHashMap<>();
        for (var a : raw) {
            if (a == null) continue;
            String rutNorm = normalizarRut(a.rut());
            if (rutNorm.isBlank() || rutNorm.equals(titularNorm)) continue;
            String nom = a.nombre() != null ? a.nombre().trim() : "";
            String ape = a.apellido() != null ? a.apellido().trim() : "";
            if (nom.isBlank() || ape.isBlank()) {
                throw new MobileException(HttpStatus.BAD_REQUEST, "ACOMPANANTE_INVALIDO", "Cada acompañante debe tener RUT/documento, nombre y apellido.");
            }
            unicos.putIfAbsent(rutNorm, new AcompananteSolicitud(rutNorm, nom, ape, a.fechaNacimiento(), a.nacionalidad()));
        }
        return new java.util.ArrayList<>(unicos.values());
    }

    private com.SkyWay.modules.pasajero.domain.model.Pasajero asegurarAcompanante(AcompananteSolicitud acomp) {
        String cleanRut = normalizarRut(acomp.rut());
        var existing = pasajeros.findById(cleanRut);
        if (existing.isPresent()) {
            return existing.get();
        }
        LocalDate fnac = acomp.fechaNacimiento() != null ? acomp.fechaNacimiento() : LocalDate.of(1995, 1, 1);
        String nac = (acomp.nacionalidad() != null && !acomp.nacionalidad().isBlank()) ? acomp.nacionalidad().trim() : "Chilena";
        if (em != null) {
            em.createNativeQuery("""
                INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
                VALUES (:rut, :nombre, :apellido, NULL, 'Sin teléfono', :doc, :fnac, NULL, NOW())
                ON CONFLICT (RUT) DO NOTHING
            """)
            .setParameter("rut", cleanRut)
            .setParameter("nombre", acomp.nombre())
            .setParameter("apellido", acomp.apellido())
            .setParameter("doc", cleanRut)
            .setParameter("fnac", java.sql.Date.valueOf(fnac))
            .executeUpdate();

            em.createNativeQuery("""
                INSERT INTO Pasajero (RUT, Tipo_Documento, Numero_Documento, Fecha_Nacimiento, Nacionalidad)
                VALUES (:rut, 'RUT', :doc, :fnac, :nac)
                ON CONFLICT (RUT) DO NOTHING
            """)
            .setParameter("rut", cleanRut)
            .setParameter("doc", cleanRut)
            .setParameter("fnac", java.sql.Date.valueOf(fnac))
            .setParameter("nac", nac)
            .executeUpdate();

            em.createNativeQuery("""
                INSERT INTO RolUsuario (id_rol, rut_usuario)
                SELECT id_rol, :rut FROM Roles WHERE nombre = 'Pasajero'
                ON CONFLICT (id_rol, rut_usuario) DO NOTHING
            """)
            .setParameter("rut", cleanRut)
            .executeUpdate();
        }
        return pasajeros.findById(cleanRut).orElseThrow(() ->
                new MobileException(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_ACOMPANANTE", "No se pudo registrar al acompañante " + cleanRut));
    }

    private static int precioEquipajeCatalogo(ItemEquipajeSolicitud item) {
        if (item == null) return 15000;
        if (Integer.valueOf(3).equals(item.idTipoEquipaje())) return 30000;
        if (Integer.valueOf(2).equals(item.idTipoEquipaje())) return 22000;
        if (Integer.valueOf(1).equals(item.idTipoEquipaje())) return 15000;
        String tipo = item.tipo() != null ? item.tipo().toUpperCase(Locale.ROOT) : "";
        if (tipo.contains("23")) return 30000;
        if (tipo.contains("15") || tipo.contains("BODEGA")) return 22000;
        return 15000;
    }

    private java.util.Set<Integer> vuelosPermitidos(Integer idItinIda, Integer idItinRegreso) {
        java.util.Set<Integer> validos = new java.util.HashSet<>();
        if (em == null) return validos;
        List<Integer> itins = new java.util.ArrayList<>();
        if (idItinIda != null) itins.add(idItinIda);
        if (idItinRegreso != null) itins.add(idItinRegreso);
        if (itins.isEmpty()) return validos;
        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery("SELECT DISTINCT iv.id_vuelo FROM itinerario_vuelo iv WHERE iv.id_itinerario IN (:ids)")
                .setParameter("ids", itins)
                .getResultList();
        for (Number n : rows) {
            if (n != null) validos.add(n.intValue());
        }
        return validos;
    }

    private void retenerAsientosEnCache(List<AsientoSeleccionadoSolicitud> asientos, String sessionKey) {
        if (asientoCacheService == null || asientos == null || asientos.isEmpty()) return;
        List<AsientoSeleccionadoSolicitud> retenidos = new java.util.ArrayList<>();
        for (var sel : asientos) {
            if (sel.idVuelo() == null || sel.idAsiento() == null) continue;
            var dto = new com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO();
            dto.setIdAsiento(sel.idAsiento());
            var res = asientoCacheService.seleccionarOliberarAsiento(sel.idVuelo(), dto, sessionKey);
            if (res == com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService.ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE
                    || res == com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService.ResultadoSeleccion.FALLO_BLOQUEADO_POR_OTRO) {
                liberarAsientosEnCache(retenidos, sessionKey);
                throw new MobileException(HttpStatus.CONFLICT, "ASIENTO_OCUPADO",
                        "Uno de los asientos seleccionados está siendo reservado por otro pasajero.");
            }
            retenidos.add(sel);
        }
    }

    private void liberarAsientosEnCache(List<AsientoSeleccionadoSolicitud> asientos, String sessionKey) {
        if (asientoCacheService == null || asientos == null) return;
        for (var sel : asientos) {
            if (sel.idVuelo() == null || sel.idAsiento() == null) continue;
            var dto = new com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO();
            dto.setIdAsiento(sel.idAsiento());
            try {
                if (asientoCacheService.validarPertenenciaYSeleccion(sel.idVuelo(), sel.idAsiento(), sessionKey)) {
                    asientoCacheService.seleccionarOliberarAsiento(sel.idVuelo(), dto, sessionKey);
                }
            } catch (Exception ignored) {}
        }
    }

    private void confirmarAsientosEnCache(List<AsientoSeleccionadoSolicitud> asientos) {
        if (asientoCacheService == null || asientos == null || asientos.isEmpty()) return;
        java.util.Map<Integer, List<Integer>> porVuelo = new java.util.HashMap<>();
        for (var sel : asientos) {
            if (sel.idVuelo() != null && sel.idAsiento() != null) {
                porVuelo.computeIfAbsent(sel.idVuelo(), k -> new java.util.ArrayList<>()).add(sel.idAsiento());
            }
        }
        porVuelo.forEach((vId, seatIds) -> {
            try {
                asientoCacheService.confirmarReservaDefinitiva(vId, seatIds);
            } catch (Exception ignored) {}
        });
    }

    private static String normalizarRut(String raw) {
        if (raw == null) return "";
        String clean = raw.replace(".", "").trim().toUpperCase(Locale.ROOT);
        return clean.length() > 12 ? clean.substring(0, 12) : clean;
    }

    private static String normalizarIata(String value, String campo) {
        String result = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!result.matches("[A-Z]{3}")) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "IATA_INVALIDO", "El aeropuerto de " + campo + " no es válido.");
        }
        return result;
    }
}
