package com.SkyWay.mobile.service;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.config.pago.SolicitudPagoDTO;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.notificacion.domain.repository.NotificacionRepository;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.modules.reservaasiento.domain.service.ReservaAsientoService;
import com.SkyWay.util.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import static com.SkyWay.mobile.service.MobileReadService.*;

@Service
@Transactional(readOnly=true)
public class MobileOperationsService {
    private final ReservaRepository reservas;
    private final NotificacionRepository notifications;
    private final MobileReadService read;
    private final MobileAuthService auth;
    private final Clock clock;

    @PersistenceContext
    private EntityManager em;

    @Autowired(required = false)
    private ReservaAsientoService reservaAsientoService;

    @Autowired(required = false)
    private AsientoCacheService asientoCacheService;

    @Autowired(required = false)
    private PagoFactoryService pagoFactoryService;

    public record SolicitudPagoPendienteCambio(
            String rutTitular,
            int idReserva,
            int idVuelo,
            int idAsientoNuevo,
            String rutPasajero,
            int idMetodoPago,
            int monto,
            int idAsientoOriginal,
            int idReservaAsiento
    ) {}

    private final java.util.concurrent.ConcurrentHashMap<String, SolicitudPagoPendienteCambio> pagosPendientesCambio =
            new java.util.concurrent.ConcurrentHashMap<>();

    public MobileOperationsService(ReservaRepository reservas,NotificacionRepository notifications,MobileReadService read,MobileAuthService auth,Clock clock) {
        this.reservas=reservas; this.notifications=notifications; this.read=read; this.auth=auth; this.clock=clock;
    }
    private String reason(DetalleReserva detail,VueloDto flight) {
        String estado = detail.estadoReserva() != null && detail.estadoReserva().descripcion() != null
                ? detail.estadoReserva().descripcion().toLowerCase() : "";
        if (estado.contains("cancelad")) return "La reserva se encuentra cancelada. No es posible cambiar asientos.";
        if(!operable(flight)) return "El vuelo no está disponible para cambios.";
        LocalDateTime departure=flight.fechaHoraSalida();
        if(departure==null || !departure.isAfter(LocalDateTime.now(clock))) return "El vuelo ya salió o no tiene horario definido.";
        // Elegir el primer asiento no es un "cambio" y no debe heredar las restricciones
        // comerciales de cambio de asiento. Estas reglas se aplican solo cuando ya existe uno.
        if(flight.asiento()!=null) {
            var fares=detail.itinerarios().stream().filter(i->i.vuelos().stream().anyMatch(v->v.vuelo().idVuelo().equals(flight.idVuelo()))).map(ItinerarioDto::tarifa).toList();
            if(fares.isEmpty()) return "La tarifa no está definida.";
            for(var fare:fares) {
                boolean allowed = fare.caracteristicas().stream().anyMatch(c ->
                        c.nombre() != null && c.nombre().toLowerCase().contains("cambio") && c.nombre().toLowerCase().contains("asiento")
                                && Boolean.TRUE.equals(c.valorBool()));
                if(!allowed) {
                    if (fare.nombre() != null && fare.nombre().toLowerCase().contains("básic")) {
                        return "La tarifa básica no permite cambios de asiento.";
                    }
                    return "Tu tarifa ("+fare.nombre()+") no autoriza cambios de asiento.";
                }

                // El plazo límite de la tarifa aplica siempre (también con check-in realizado)
                for(var c:fare.caracteristicas()) {
                    if(c.nombre() != null && (c.nombre().toLowerCase().contains("plazo") || (c.nombre().toLowerCase().contains("horas") && c.nombre().toLowerCase().contains("cambio")))) {
                        if(c.valorInt()==null || c.valorInt()<0) return "La anticipación mínima de la tarifa requiere revisión.";
                        if(LocalDateTime.now(clock).plusHours(c.valorInt()).isAfter(departure)) {
                            if (fare.nombre() != null && fare.nombre().toLowerCase().contains("flex")) {
                                return "Solo puedes cambiar el asiento hasta 48 horas antes del vuelo con tarifa flexible.";
                            } else if (fare.nombre() != null && fare.nombre().toLowerCase().contains("prem")) {
                                return "Solo puedes cambiar el asiento hasta 24 horas antes del vuelo con tarifa premium.";
                            }
                            return "El plazo límite estipulado por tu tarifa ("+fare.nombre()+") para cambiar asientos ha finalizado.";
                        }
                    }
                }
            }
        }
        return null;
    }
    /** Jerarquía de cabinas: Económica (1) < Ejecutiva (2) < Primera (3). */
    public static int rangoClase(String clase) {
        if (clase == null) return 0;
        String c = java.text.Normalizer.normalize(clase, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
        if (c.contains("primera") || c.contains("first")) return 3;
        if (c.contains("ejecutiva") || c.contains("business")) return 2;
        if (c.contains("economica") || c.contains("economy") || c.contains("turista")) return 1;
        return 0;
    }
    private static boolean operable(VueloDto flight) {
        if(cancelado(flight)||flight.estado().idEstadoVuelo()==null) return false;
        String state=Objects.toString(flight.estado().estado(),"");
        String desc=Objects.toString(flight.estado().descripcion(),"");
        return !"Finalizado".equalsIgnoreCase(state)&&!"En vuelo".equalsIgnoreCase(desc)&&!"Aterrizado".equalsIgnoreCase(desc);
    }
    public MapaAsientos mapa(String rut,int id,int flightId) {
        var detail=read.detalle(rut,id); var flight=vuelo(detail,flightId); String reason=reason(detail,flight);

        final List<Object[]> rawMapRows = reservas.mobileMapa(flightId, id, rut);

        Integer precioActual = null;
        if (flight.asiento() != null) {
            precioActual = flight.asiento().precio();
            if (precioActual == null && flight.asiento().idClase() != null) {
                precioActual = rawMapRows.stream()
                        .filter(r -> Objects.equals(number(r[2]), flight.asiento().idClase()) && r[4] != null)
                        .map(r -> number(r[4]))
                        .findFirst().orElse(null);
            }
        }

        final Integer basePrice = precioActual;
        final int precioEconomicaBase = rawMapRows.stream()
                .filter(r -> rangoClase(text(r[3])) == 1 && r[4] != null)
                .map(r -> number(r[4]))
                .min(Integer::compareTo)
                .orElse(0);

        int pctCargo = 15;
        if (detail.itinerarios() != null) {
            for (var it : detail.itinerarios()) {
                boolean perteneceAlVuelo = it.vuelos() != null && it.vuelos().stream()
                        .anyMatch(c -> c.vuelo() != null && Objects.equals(c.vuelo().idVuelo(), flightId));
                if (!perteneceAlVuelo) continue;
                if (it.tarifa() != null && it.tarifa().caracteristicas() != null) {
                    for (var c : it.tarifa().caracteristicas()) {
                        if (c.nombre() != null && (c.nombre().toLowerCase().contains("porcentaje") || c.nombre().toLowerCase().contains("cargo"))) {
                            if (c.valorInt() != null) {
                                pctCargo = c.valorInt();
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }
        final int porcentajeCargoTarifa = pctCargo;

        String nombreModeloAvion = "Aeronave Comercial";
        boolean avionDoblePasillo = false;
        List<ConfiguracionCabinaItemDto> configuracionesCabina = new ArrayList<>();

        if (em != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Object[]> cfgRows = em.createNativeQuery("""
                    SELECT m.nombre, cc.id_configuracion, ca.descripcion, cc.fila_inicio, cc.fila_fin,
                           cc.distribucion_columnas, cc.letras_columnas, COALESCE(cc.es_salida_emergencia, FALSE)
                    FROM vuelo v
                    JOIN avion av ON av.id_avion = v.id_avion
                    JOIN modelo_avion m ON m.id_modelo = av.id_modelo
                    LEFT JOIN configuracion_cabina cc ON cc.id_modelo = m.id_modelo
                    LEFT JOIN clase_asiento ca ON ca.id_clase = cc.id_clase
                    WHERE v.id_vuelo = :flightId
                    ORDER BY cc.fila_inicio ASC
                """).setParameter("flightId", flightId).getResultList();

                for (Object[] r : cfgRows) {
                    if (r[0] != null && "Aeronave Comercial".equals(nombreModeloAvion)) {
                        nombreModeloAvion = text(r[0]);
                    }
                    if (r[1] != null) {
                        Integer idCfg = number(r[1]);
                        String claseCfg = text(r[2]);
                        Integer fIni = number(r[3]);
                        Integer fFin = number(r[4]);
                        String dist = text(r[5]);
                        String letras = text(r[6]);
                        Boolean esEmerg = r[7] != null ? (Boolean) r[7] : false;
                        configuracionesCabina.add(new ConfiguracionCabinaItemDto(idCfg, claseCfg, fIni, fFin, dist, letras, esEmerg));
                    }
                }

                avionDoblePasillo = configuracionesCabina.stream()
                        .anyMatch(cc -> cc.distribucionColumnas() != null &&
                                cc.distribucionColumnas().chars().filter(ch -> ch == '-').count() >= 2);
            } catch (Exception ignored) {}
        }

        var seats = rawMapRows.stream().map(r -> {
            Integer seatId = number(r[0]);
            String numSeat = text(r[1]);
            Integer classId = number(r[2]);
            String className = text(r[3]);
            Integer price = number(r[4]);
            String rawState = text(r[5]);
            String state = "disponible".equalsIgnoreCase(rawState) ? "libre" : rawState;

            Integer fila = r.length > 6 ? number(r[6]) : null;
            String letra = r.length > 7 ? text(r[7]) : null;
            Boolean esPasillo = r.length > 8 && r[8] != null ? (Boolean) r[8] : false;
            Boolean esEmergencia = r.length > 9 && r[9] != null ? (Boolean) r[9] : false;

            if (fila == null) {
                String d = numSeat.replaceAll("[^0-9]", "");
                fila = d.isEmpty() ? 1 : Integer.parseInt(d);
            }
            if (letra == null || letra.isBlank()) {
                String l = numSeat.replaceAll("[0-9]", "").trim();
                letra = l.isEmpty() ? numSeat : l;
            }

            int diff = 0;
            int fee = 0;
            int total = 0;
            boolean isUpgrade = false;
            boolean selectable = false;

            if (reason == null && "libre".equalsIgnoreCase(state) && price != null) {
                if (flight.asiento() == null) {
                    int rankNuevo = rangoClase(className);
                    if (rankNuevo <= 1) {
                        selectable = true;
                    } else {
                        diff = Math.max(0, price - precioEconomicaBase);
                        fee = (diff > 0 && porcentajeCargoTarifa > 0) ? Math.round(diff * (porcentajeCargoTarifa / 100.0f)) : 0;
                        total = diff + fee;
                        isUpgrade = total > 0;
                        selectable = true;
                    }
                } else {
                    int rankActual = rangoClase(flight.asiento() != null ? flight.asiento().clase() : null);
                    int rankNuevo = rangoClase(className);
                    if (rankNuevo == rankActual) {
                        // Misma cabina: sin costo
                        selectable = true;
                    } else if (rankNuevo > rankActual) {
                        // Ascenso de cabina: diferencia + cargo porcentual de la tarifa
                        diff = Math.max(0, price - (basePrice == null ? 0 : basePrice));
                        fee = (diff > 0 && porcentajeCargoTarifa > 0) ? Math.round(diff * (porcentajeCargoTarifa / 100.0f)) : 0;
                        total = diff + fee;
                        isUpgrade = diff > 0;
                        selectable = true;
                    }
                    // rankNuevo < rankActual: descenso de cabina no permitido en autoservicio
                }
            }

            return new AsientoDto(seatId, numSeat, classId, className, price, state, selectable,
                    diff, fee, total, isUpgrade, fila, letra, esPasillo, esEmergencia);
        }).toList();

        return new MapaAsientos(id, flightId, reason == null, reason, flight.asiento(), basePrice,
                nombreModeloAvion, avionDoblePasillo, configuracionesCabina, seats);
    }
    @Transactional
    public MapaAsientos cambiar(String rut,int id,int flightId,int seatId) {
        return cambiar(rut, id, flightId, seatId, 1, null, null);
    }

    @Transactional
    public MapaAsientos cambiar(String rut,int id,int flightId,int seatId, Integer idMetodoPago) {
        return cambiar(rut, id, flightId, seatId, idMetodoPago, null, null);
    }

    @Transactional
    public MapaAsientos cambiar(String rut, int id, int flightId, int seatId, Integer idMetodoPago, String urlRetornoBase, String passengerRut) {
        auth.requirePassenger(rut);
        String rutPasajeroFinal = (passengerRut != null && !passengerRut.isBlank()) ? passengerRut : rut;
        reservas.mobileReserva(rut, id, true); // Mismo bloqueo que check-in: evita carreras entre ambas operaciones.
        // Autorizar el vuelo antes de bloquearlo.
        vuelo(read.detalle(rut, id), flightId);
        reservas.mobileLockVuelo(flightId);
        var map = mapa(rutPasajeroFinal, id, flightId);
        if (map.asientoActual() != null && map.asientoActual().idAsiento() == seatId) return map; // PUT idempotente.
        if (!map.cambioPermitido()) throw MobileException.conflict(map.motivo());
        var target = map.asientos().stream().filter(a -> a.idAsiento() == seatId).findFirst().orElseThrow(MobileException::notFound);
        if (!target.seleccionable()) throw MobileException.conflict("Asiento ocupado, sin precio o categoría no permitida.");

        // Si requiere pago adicional (upgrade de clase): Iniciar transacción en pasarela (Webpay Plus o PayPal)
        if (target.totalPagar() != null && target.totalPagar() > 0) {
            int metodo = (idMetodoPago != null && idMetodoPago > 0) ? idMetodoPago : 1;
            if (pagoFactoryService == null) {
                throw new MobileException(HttpStatus.INTERNAL_SERVER_ERROR, "PASARELA_NO_DISPONIBLE",
                        "El servicio de pasarela de pago no está disponible.");
            }

            String ordenCompra = "SEAT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
            String base = (urlRetornoBase != null && !urlRetornoBase.isBlank())
                    ? urlRetornoBase.trim().replaceAll("/+$", "")
                    : "http://localhost:8101";

            if (asientoCacheService != null) {
                var dto = new com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO();
                dto.setIdAsiento(seatId);
                var lockRes = asientoCacheService.seleccionarOliberarAsiento(flightId, dto, ordenCompra);
                if (lockRes == com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService.ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE
                        || lockRes == com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService.ResultadoSeleccion.FALLO_BLOQUEADO_POR_OTRO) {
                    throw MobileException.conflict("Este asiento acaba de ser seleccionado por otro pasajero.");
                }
            }

            SolicitudPagoDTO solicitud = new SolicitudPagoDTO();
            solicitud.setOrdenCompra(ordenCompra);
            solicitud.setSessionId(ordenCompra);
            solicitud.setDescripcion("Ascenso Asiento SkyWay - Reserva #" + id);

            MetodoPagoEnum metodoEnum = (metodo == 2) ? MetodoPagoEnum.PAYPAL : MetodoPagoEnum.WEBPAY;

            String rutPath = (passengerRut != null && !passengerRut.isBlank()) ? "/tickets/" + passengerRut : "";
            String returnPath = base + "/vuelos/reservas/" + id + "/vuelos/" + flightId + rutPath + "/asientos";

            if (metodoEnum == MetodoPagoEnum.PAYPAL) {
                solicitud.setMoneda("USD");
                solicitud.setMonto(BigDecimal.valueOf(target.totalPagar()).divide(BigDecimal.valueOf(950), 2, RoundingMode.HALF_UP));
                solicitud.setReturnUrl(returnPath + "?pasarela=paypal&orden=" + ordenCompra);
                solicitud.setCancelUrl(returnPath + "?pasarela=paypal&orden=" + ordenCompra + "&cancelado=true");
            } else {
                solicitud.setMoneda("CLP");
                solicitud.setMonto(BigDecimal.valueOf(target.totalPagar()));
                solicitud.setReturnUrl(returnPath + "?pasarela=webpay&orden=" + ordenCompra);
                solicitud.setCancelUrl(returnPath + "?pasarela=webpay&orden=" + ordenCompra + "&cancelado=true");
            }

            try {
                PasarelaPagoStrategy pasarela = pagoFactoryService.obtenerEstrategia(metodoEnum);
                String redirectUrl = pasarela.iniciarTransaccion(solicitud);

                int orgSeatId = (map.asientoActual() != null) ? map.asientoActual().idAsiento() : 0;
                int orgReservaAsientoId = (map.asientoActual() != null) ? map.asientoActual().idReservaAsiento() : 0;

                pagosPendientesCambio.put(ordenCompra, new SolicitudPagoPendienteCambio(
                        rut, id, flightId, seatId, rutPasajeroFinal, metodo, target.totalPagar(), orgSeatId, orgReservaAsientoId
                ));

                return new MapaAsientos(
                        map.idReserva(), map.idVuelo(), map.cambioPermitido(), map.motivo(),
                        map.asientoActual(), map.precioActual(),
                        map.nombreModeloAvion(), map.avionDoblePasillo(),
                        map.configuracionesCabina(), map.asientos(),
                        ordenCompra, redirectUrl
                );
            } catch (Exception ex) {
                liberarAsientoCambioEnCache(flightId, seatId, ordenCompra);
                Logger.logError("Error iniciando pago de ascenso en pasarela " + metodoEnum + ": " + ex.getMessage());
                throw new MobileException(HttpStatus.BAD_GATEWAY, "ERROR_PASARELA",
                        "No fue posible conectar con " + (metodo == 2 ? "PayPal" : "Webpay Plus") + ". Intenta nuevamente.");
            }
        }

        // Si no requiere pago adicional (misma clase, $0):
        if(map.asientoActual()==null) {
            if(reservas.mobileAsignarAsiento(id,flightId,rutPasajeroFinal,seatId)!=1)
                throw MobileException.conflict("No fue posible asignar el asiento. Actualiza el mapa e inténtalo otra vez.");
        } else {
            int orgSeatId = map.asientoActual().idAsiento();
            boolean exito = false;
            if (reservaAsientoService != null) {
                try {
                    exito = reservaAsientoService.cambiarAsiento(seatId, id, orgSeatId);
                } catch (Exception spEx) {
                    Logger.logWarn("Fallback a mobileCambiarAsiento tras SP en reserva " + id + ": " + spEx.getMessage());
                    exito = false;
                }
            }
            if (!exito) {
                exito = (reservas.mobileCambiarAsiento(map.asientoActual().idReservaAsiento(),id,flightId,rutPasajeroFinal,seatId)==1);
            }
            if (!exito) {
                throw MobileException.conflict("La asignación cambió. Actualiza el mapa e inténtalo otra vez.");
            }

            if (asientoCacheService != null) {
                try {
                    asientoCacheService.actualizarCambioAsiento(flightId, orgSeatId, seatId);
                } catch (Exception ignored) {}
            }
        }
        return mapa(rutPasajeroFinal,id,flightId);
    }

    @Transactional
    public MapaAsientos confirmarPagoCambio(String rut, ConfirmarPagoCambioAsientoRequest request) {
        if (auth != null) auth.requirePassenger(rut);
        SolicitudPagoPendienteCambio pendiente = pagosPendientesCambio.remove(request.ordenCompra());
        if (pendiente == null || !normRutOp(pendiente.rutTitular()).equals(normRutOp(rut))) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "PAGO_NO_ENCONTRADO",
                    "No se encontró el cambio de asiento pendiente para confirmar.");
        }

        MetodoPagoEnum metodoEnum = ("paypal".equalsIgnoreCase(request.pasarela()) || "2".equals(request.pasarela()))
                ? MetodoPagoEnum.PAYPAL
                : MetodoPagoEnum.WEBPAY;

        if (pagoFactoryService == null) {
            liberarAsientoCambioEnCache(pendiente.idVuelo(), pendiente.idAsientoNuevo(), request.ordenCompra());
            throw new MobileException(HttpStatus.INTERNAL_SERVER_ERROR, "PASARELA_NO_DISPONIBLE",
                    "El servicio de pasarela de pago no está disponible.");
        }

        PasarelaPagoStrategy estrategia = pagoFactoryService.obtenerEstrategia(metodoEnum);
        boolean autorizado = estrategia.confirmarPago(request.token());
        if (!autorizado) {
            liberarAsientoCambioEnCache(pendiente.idVuelo(), pendiente.idAsientoNuevo(), request.ordenCompra());
            throw new MobileException(HttpStatus.BAD_REQUEST, "PAGO_RECHAZADO",
                    "La transacción no fue autorizada o fue rechazada por " + (metodoEnum == MetodoPagoEnum.PAYPAL ? "PayPal" : "Webpay Plus") + ".");
        }

        // Ejecutar cambio de asiento atómico
        reservas.mobileReserva(rut, pendiente.idReserva(), true);
        reservas.mobileLockVuelo(pendiente.idVuelo());

        boolean exito = false;
        if (pendiente.idAsientoOriginal() == 0) {
            exito = (reservas.mobileAsignarAsiento(pendiente.idReserva(), pendiente.idVuelo(), pendiente.rutPasajero(), pendiente.idAsientoNuevo()) == 1);
        } else {
            if (reservaAsientoService != null) {
                try {
                    exito = reservaAsientoService.cambiarAsiento(pendiente.idAsientoNuevo(), pendiente.idReserva(), pendiente.idAsientoOriginal());
                } catch (Exception spEx) {
                    Logger.logWarn("Fallback a mobileCambiarAsiento tras SP en confirmación pago reserva " + pendiente.idReserva() + ": " + spEx.getMessage());
                    exito = false;
                }
            }
            if (!exito) {
                exito = (reservas.mobileCambiarAsiento(pendiente.idReservaAsiento(), pendiente.idReserva(), pendiente.idVuelo(), pendiente.rutPasajero(), pendiente.idAsientoNuevo()) == 1);
            }
        }
        if (!exito) {
            liberarAsientoCambioEnCache(pendiente.idVuelo(), pendiente.idAsientoNuevo(), request.ordenCompra());
            throw MobileException.conflict("No fue posible reasignar la butaca tras el pago. Contacta a soporte.");
        }

        // Registrar pago de la diferencia en la tabla Pago
        registrarPagoDiferencia(pendiente.idReserva(), pendiente.monto(), pendiente.idMetodoPago());

        // Sincronizar websocket y cache
        if (asientoCacheService != null) {
            try {
                asientoCacheService.actualizarCambioAsiento(
                        pendiente.idVuelo(),
                        pendiente.idAsientoOriginal() > 0 ? pendiente.idAsientoOriginal() : null,
                        pendiente.idAsientoNuevo()
                );
            } catch (Exception ignored) {}
        }

        return mapa(pendiente.rutPasajero(), pendiente.idReserva(), pendiente.idVuelo());
    }

    private void liberarAsientoCambioEnCache(int idVuelo, int idAsiento, String ordenCompra) {
        if (asientoCacheService == null) return;
        try {
            if (asientoCacheService.validarPertenenciaYSeleccion(idVuelo, idAsiento, ordenCompra)) {
                var dto = new com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO();
                dto.setIdAsiento(idAsiento);
                asientoCacheService.seleccionarOliberarAsiento(idVuelo, dto, ordenCompra);
            }
        } catch (Exception ignored) {}
    }

    private static String normRutOp(String r) {
        return r == null ? "" : r.replace(".", "").trim().toUpperCase(Locale.ROOT);
    }

    private void registrarPagoDiferencia(int idReserva, int monto, int idMetodoPago) {
        if (em == null) return;
        Reserva r = em.find(Reserva.class, idReserva);
        if (r == null) throw MobileException.notFound();
        MetodoPago mp = em.find(MetodoPago.class, idMetodoPago);
        if (mp == null) {
            mp = em.find(MetodoPago.class, 1);
        }
        Pago pago = new Pago();
        pago.setFecha(new Timestamp(System.currentTimeMillis()));
        pago.setMontoPagado(BigDecimal.valueOf(monto));
        pago.setReserva(r);
        pago.setMetodoPago(mp);
        em.persist(pago);
    }
    @Transactional
    public CheckinDto checkin(String rut,int id) {
        if (auth != null) auth.requirePassenger(rut);
        reservas.mobileReserva(rut,id,true);
        var detail=read.detalle(rut,id);
        if("Cancelada".equalsIgnoreCase(detail.estadoReserva().descripcion())) throw MobileException.conflict("La reserva está cancelada.");
        if(detail.checkin()!=null) return detail.checkin();
        if(!detail.titular()) throw new MobileException(HttpStatus.FORBIDDEN,"SOLO_TITULAR","El titular debe registrar el check-in de toda la reserva.");
        String desc = detail.estadoReserva().descripcion();
        if(!"Confirmada".equalsIgnoreCase(desc) && !"Check-in realizado".equalsIgnoreCase(desc)) throw MobileException.conflict("La reserva debe estar confirmada.");
        var flights=vuelos(detail);
        if(flights.isEmpty()) throw MobileException.conflict("La reserva no contiene vuelos.");
        // Orden estable de bloqueos, también frente al cambio de asientos.
        flights.stream().map(VueloDto::idVuelo).sorted().forEach(reservas::mobileLockVuelo);
        detail=read.detalle(rut,id); flights=vuelos(detail);
        var now=LocalDateTime.now(clock);

        LocalDateTime primerSalida = flights.stream()
                .map(VueloDto::fechaHoraSalida)
                .filter(Objects::nonNull)
                .sorted()
                .findFirst()
                .orElse(null);

        if (primerSalida != null) {
            LocalDateTime aperturaCheckin = primerSalida.minusHours(48);
            LocalDateTime cierreCheckin = primerSalida.minusMinutes(60);

            if (now.isBefore(aperturaCheckin)) {
                throw MobileException.conflict("El check-in se habilitará 48 horas antes de la salida del vuelo.");
            }
            if (now.isAfter(cierreCheckin)) {
                throw MobileException.conflict("El check-in cerró 60 minutos antes de la salida del vuelo.");
            }
        }

        for(var flight:flights) if(!operable(flight)||flight.fechaHoraSalida()==null||!flight.fechaHoraSalida().isAfter(now))
            throw MobileException.conflict("El check-in de la reserva debe registrarse antes de la salida de todos sus vuelos operativos.");
        if(!reservas.mobileFaltanAsientos(id,now).isEmpty()) throw MobileException.conflict("Cada pasajero debe tener exactamente un asiento del avión asignado en cada vuelo.");
        reservas.mobileInsertCheckin(id,now);
        return read.checkin(id);
    }
    @Transactional
    public NotificacionDto leer(String rut,int id) {
        auth.requirePassenger(rut);
        var n=notifications.findByIdNotificacionAndUsuario_Rut(id,rut).orElseThrow(MobileException::notFound);
        n.setLeido(true); notifications.save(n);
        return notification(n);
    }
}
