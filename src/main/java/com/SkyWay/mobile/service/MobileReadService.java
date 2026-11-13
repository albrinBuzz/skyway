package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.notificacion.domain.repository.NotificacionRepository;
import com.SkyWay.modules.notificacion.domain.model.Notificacion;
import com.SkyWay.util.BarcodeGeneratorUtil;
import com.SkyWay.util.BoardingPassSecurityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly=true)
public class MobileReadService {
    private final ReservaRepository reservas;
    private final NotificacionRepository notificaciones;
    private final MobileAuthService auth;
    private final Clock clock;

    @PersistenceContext
    private EntityManager em;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.SkyWay.modules.reserva.domain.service.ETicketPdfGeneratorService eTicketPdfGeneratorService;

    public MobileReadService(ReservaRepository reservas, NotificacionRepository notificaciones, MobileAuthService auth, Clock clock) {
        this.reservas=reservas; this.notificaciones=notificaciones; this.auth=auth; this.clock=clock;
    }
    public static Integer number(Object o) { return o == null ? null : ((Number)o).intValue(); }
    public static String text(Object o) { return o == null ? null : o.toString(); }
    public static LocalDateTime date(Object o) {
        if (o == null) return null;
        return o instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime)o;
    }
    public static Double decimal(Object o) {
        return o == null ? null : ((Number)o).doubleValue();
    }
    private static AeropuertoDto airport(Object[] r,int start) {
        return r[start] == null ? null : new AeropuertoDto(number(r[start]),text(r[start+1]),text(r[start+2]),text(r[start+3]),text(r[start+4]),decimal(r[start+5]),decimal(r[start+6]));
    }
    public static void pagination(int page,int size) {
        if(page<0 || page>100000 || size<1 || size>100)
            throw new MobileException(HttpStatus.BAD_REQUEST,"PAGINACION_INVALIDA","Usa pagina entre 0 y 100000 y tamano entre 1 y 100.");
    }
    public Pagina<ResumenReserva> listar(String rut,String grupo,int page,int size) {
        auth.requirePassenger(rut); pagination(page,size);
        if(!List.of("activas","historial").contains(grupo))
            throw new MobileException(HttpStatus.BAD_REQUEST,"GRUPO_INVALIDO","grupo debe ser activas o historial.");
        boolean active=grupo.equals("activas"); var now=LocalDateTime.now(clock);
        var data=reservas.mobileReservas(rut,active,now,size,page*size).stream().map(r -> {
            boolean checkinHecho = r[3] != null && r[3].toString().toLowerCase(Locale.ROOT).contains("check-in");
            return new ResumenReserva(
                number(r[0]),date(r[1]),new EstadoReservaDto(number(r[2]),text(r[3])),(BigDecimal)r[4],active,
                date(r[5]),text(r[6]),text(r[7]), checkinHecho);
        }).toList();
        return Pagina.of(data,page,size,reservas.mobileCountReservas(rut,active,now));
    }
    public DetalleReserva detalle(String rut,int id) {
        auth.requirePassenger(rut);
        Object[] booking=reservas.mobileReserva(rut,id,false);
        Map<Integer,List<SegmentoDto>> segments=new HashMap<>();
        for(Object[] r:reservas.mobileSegmentos(id)) {
            PuertaDto gate=r[19]==null?null:new PuertaDto(number(r[19]),text(r[20]),text(r[21]),number(r[22]));
            segments.computeIfAbsent(number(r[0]),k->new ArrayList<>()).add(new SegmentoDto(
                    number(r[1]),number(r[2]),date(r[3]),date(r[4]),airport(r,5),airport(r,12),gate));
        }
        Map<Integer,AsientoAsignado> seats=new HashMap<>();
        for(Object[] r:reservas.mobileAsignados(id,rut)) {
            if(seats.put(number(r[0]),new AsientoAsignado(number(r[1]),number(r[2]),text(r[3]),number(r[4]),text(r[5]),r.length>6?number(r[6]):null))!=null)
                throw MobileException.conflict("La reserva tiene más de un asiento para el mismo pasajero y vuelo. Solicita su revisión.");
        }
        Map<Integer,List<ConexionDto>> connections=new HashMap<>();
        for(Object[] r:reservas.mobileConexiones(id)) {
            int flight=number(r[5]);
            VueloDto v=new VueloDto(flight,text(r[6]),date(r[7]),date(r[8]),number(r[9]),
                    new EstadoVueloDto(number(r[10]),text(r[11]),text(r[12])),segments.getOrDefault(flight,List.of()),seats.get(flight),text(r[13]));
            connections.computeIfAbsent(number(r[0]),k->new ArrayList<>()).add(new ConexionDto(number(r[1]),number(r[2]),text(r[3]),text(r[4]),v));
        }
        Map<Integer,List<CaracteristicaDto>> features=new HashMap<>();
        for(Object[] r:reservas.mobileCaracteristicas(id)) features.computeIfAbsent(number(r[0]),k->new ArrayList<>()).add(
                new CaracteristicaDto(number(r[1]),number(r[2]),text(r[3]),text(r[4]),text(r[5]),text(r[6]),(Boolean)r[7],number(r[8])));
        var itineraries=reservas.mobileItinerarios(id).stream().map(r->new ItinerarioDto(
                number(r[0]),number(r[1]),date(r[2]),date(r[3]),number(r[4]),number(r[5]),airport(r,6),airport(r,13),
                new TarifaDto(number(r[20]),number(r[21]),text(r[22]),(BigDecimal)r[23],features.getOrDefault(number(r[21]),List.of())),
                connections.getOrDefault(number(r[1]),List.of()))).toList();
        var bags=reservas.mobileEquipajes(id,rut).stream().map(r->new EquipajeDto(number(r[0]),(BigDecimal)r[1],text(r[2]),text(r[3]),number(r[4]),text(r[5]))).toList();

        CheckinDto chk = checkin(id);
        boolean checkinDisponible = false;
        LocalDateTime fechaAperturaCheckin = null;
        LocalDateTime fechaCierreCheckin = null;
        String estadoVentanaCheckin = "NO_DISPONIBLE";
        String mensajeCheckin = null;

        if (chk != null) {
            estadoVentanaCheckin = "COMPLETADO";
            mensajeCheckin = "Check-in completado con éxito.";
        } else {
            LocalDateTime primerSalida = itineraries.stream()
                    .flatMap(it -> it.vuelos().stream())
                    .map(c -> c.vuelo().fechaHoraSalida())
                    .filter(Objects::nonNull)
                    .sorted()
                    .findFirst()
                    .orElse(null);

            if (primerSalida != null) {
                fechaAperturaCheckin = primerSalida.minusHours(48);
                fechaCierreCheckin = primerSalida.minusMinutes(60);
                LocalDateTime ahora = LocalDateTime.now(clock);

                if (ahora.isBefore(fechaAperturaCheckin)) {
                    estadoVentanaCheckin = "NO_DISPONIBLE_AUN";
                    mensajeCheckin = "El check-in se habilitará 48 horas antes del vuelo.";
                } else if (ahora.isAfter(fechaCierreCheckin)) {
                    estadoVentanaCheckin = "CERRADO";
                    mensajeCheckin = "El check-in ha cerrado (cierra 60 minutos antes del vuelo).";
                } else {
                    checkinDisponible = true;
                    estadoVentanaCheckin = "HABILITADO";
                    mensajeCheckin = "Check-in disponible ahora. Complétalo para activar tu tarjeta de embarque.";
                }
            }
        }

        return new DetalleReserva(number(booking[0]),date(booking[1]),new EstadoReservaDto(number(booking[2]),text(booking[3])),
                (BigDecimal)booking[4],rut.equals(booking[5]),itineraries,bags,chk,
                checkinDisponible, fechaAperturaCheckin, fechaCierreCheckin, estadoVentanaCheckin, mensajeCheckin);
    }
    CheckinDto checkin(int id) {
        return reservas.mobileCheckins(id).stream().map(r->new CheckinDto(number(r[0]),number(r[1]),date(r[2]),text(r[3]))).findFirst().orElse(null);
    }
    public static List<VueloDto> vuelos(DetalleReserva detail) {
        return detail.itinerarios().stream().flatMap(i->i.vuelos().stream()).map(ConexionDto::vuelo)
                .collect(Collectors.toMap(VueloDto::idVuelo,v->v,(a,b)->a,LinkedHashMap::new)).values().stream().toList();
    }
    public static VueloDto vuelo(DetalleReserva detail,int id) {
        return vuelos(detail).stream().filter(v->v.idVuelo()==id).findFirst().orElseThrow(MobileException::notFound);
    }
    public VueloDto vuelo(String rut,int idReserva,int idVuelo) { return vuelo(detalle(rut,idReserva),idVuelo); }
    public Inicio inicio(String rut) {
        Perfil profile=auth.perfil(rut);
        var next=reservas.mobileNext(rut,LocalDateTime.now(clock));
        ProximoVuelo flight=next.isEmpty()?null:new ProximoVuelo(number(next.get(0)[0]),vuelo(rut,number(next.get(0)[0]),number(next.get(0)[1])));
        return new Inicio(profile,flight,notificaciones.countByUsuario_RutAndLeidoFalse(rut));
    }
    static NotificacionDto notification(Notificacion n) {
        return new NotificacionDto(n.getIdNotificacion(),n.getTitulo(),n.getMensaje(),n.getLeido(),date(n.getFecha()));
    }
    public Pagina<NotificacionDto> notificaciones(String rut,int page,int size) {
        auth.requirePassenger(rut); pagination(page,size);
        var data=notificaciones.findByUsuario_RutOrderByFechaDescIdNotificacionDesc(rut,PageRequest.of(page,size));
        return Pagina.of(data.stream().map(MobileReadService::notification).toList(),page,size,data.getTotalElements());
    }

    public PaseAbordar pase(String rut, int idReserva, int flightId) {
        var detail = detalle(rut, idReserva);
        if (detail.checkin() == null) {
            throw new MobileException(HttpStatus.CONFLICT, "CHECKIN_PENDIENTE",
                    "La tarjeta de embarque solo está disponible después de realizar el check-in.");
        }

        var flight = vuelo(detail, flightId);
        if (flight.asiento() == null) throw MobileException.conflict("No tienes asiento asignado.");

        String pnr = resolverCodigoPnr(idReserva);
        String numVuelo = flight.numeroVuelo() != null ? flight.numeroVuelo() : "SW-" + flightId;
        String bcbp = BoardingPassSecurityService.generarCodigoSeguro(pnr, numVuelo, flight.asiento().numeroAsiento(), rut);
        String barcodeBase64 = BarcodeGeneratorUtil.generarPdf417DataUrl(bcbp);

        return new PaseAbordar(detail.checkin(), auth.perfil(rut).usuario(), flight, bcbp, false, pnr, bcbp, barcodeBase64);
    }

    public byte[] descargarBoardingPassPdf(String rut, int idReserva) {
        var detail = detalle(rut, idReserva);
        if (detail.checkin() == null) {
            throw new MobileException(HttpStatus.CONFLICT, "CHECKIN_PENDIENTE",
                    "La tarjeta de embarque solo está disponible para descarga después de realizar el check-in.");
        }
        if (eTicketPdfGeneratorService == null) {
            throw new MobileException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF_SERVICE_UNAVAILABLE",
                    "El generador de PDF no se encuentra disponible.");
        }
        var reserva = reservas.findById(idReserva).orElseThrow(MobileException::notFound);
        byte[] pdf = eTicketPdfGeneratorService.generarPdfBoardingPasses(reserva);
        if (pdf == null || pdf.length == 0) {
            throw new MobileException(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_PDF",
                    "No se pudo generar la tarjeta de embarque en formato PDF.");
        }
        return pdf;
    }

    private String resolverCodigoPnr(int idReserva) {
        if (em != null) {
            try {
                Object val = em.createNativeQuery("SELECT codigo_reserva FROM reserva WHERE id_reserva = :id")
                        .setParameter("id", idReserva).getSingleResult();
                String pnr = text(val);
                if (pnr != null && !pnr.isBlank()) return pnr.trim().toUpperCase();
            } catch (Exception ignored) {}
        }
        return "RES-" + idReserva;
    }

    public static boolean cancelado(VueloDto v) {
        String status=Objects.toString(v.estado().estado(),"").toLowerCase(Locale.ROOT);
        String desc=Objects.toString(v.estado().descripcion(),"").toLowerCase(Locale.ROOT);
        return List.of("cancelado","inactivo").contains(status)||List.of("cancelado","vuelo cancelado").contains(desc);
    }

    public ConsultaPnrResponse consultarPorPnr(String codigoPnr, String apellido) {
        if (em == null || codigoPnr == null || apellido == null) {
            throw MobileException.notFound();
        }
        String pnr = codigoPnr.trim().toUpperCase(Locale.ROOT);
        String ape = apellido.trim().toLowerCase(Locale.ROOT);
        if (pnr.isBlank() || ape.isBlank()) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "DATOS_INCOMPLETOS", "Ingresa el código PNR y tu apellido.");
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT r.id_reserva, COALESCE(r.codigo_reserva, 'RES-' || r.id_reserva),
                   COALESCE(er.descripcion, 'Confirmada'),
                   u.rut, TRIM(COALESCE(u.nombre, '') || ' ' || COALESCE(u.apellido, ''))
            FROM reserva r
            LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
            JOIN pasajero p ON (p.rut = r.rut_pasajero OR EXISTS (
                SELECT 1 FROM pasajero_reserva pr WHERE pr.id_reserva = r.id_reserva AND pr.rut = p.rut
            ))
            JOIN usuario u ON u.rut = p.rut
            WHERE (UPPER(TRIM(COALESCE(r.codigo_reserva, ''))) = :pnr
                   OR (:pnr ~ '^[0-9]+$' AND r.id_reserva = CAST(:pnrNum AS INT)))
              AND LOWER(TRIM(COALESCE(u.apellido, ''))) = :apellido
            ORDER BY CASE WHEN u.rut = r.rut_pasajero THEN 0 ELSE 1 END, r.id_reserva DESC
            LIMIT 1
        """)
        .setParameter("pnr", pnr)
        .setParameter("pnrNum", pnr.matches("\\d+") ? Integer.parseInt(pnr) : -1)
        .setParameter("apellido", ape)
        .getResultList();

        if (rows.isEmpty()) {
            throw new MobileException(HttpStatus.NOT_FOUND, "RESERVA_NO_ENCONTRADA",
                    "No encontramos ninguna reserva con ese código y apellido.");
        }

        Object[] row = rows.get(0);
        int idReserva = number(row[0]);
        String pnrOficial = text(row[1]);
        String estadoReserva = text(row[2]);
        String rutMatch = text(row[3]);
        String nombrePasajero = text(row[4]);

        var chk = checkin(idReserva);
        boolean checkinHecho = chk != null;

        @SuppressWarnings("unchecked")
        List<Object[]> flightRows = em.createNativeQuery("""
            SELECT v.id_vuelo, v.numero_vuelo, v.fecha_hora_salida,
                   ao.codigo_iata, ao.ciudad, ad.codigo_iata, ad.ciudad,
                   pe.codigo_puerta, pe.terminal, a.numero_asiento
            FROM reserva_itinerario ri
            JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
            JOIN vuelo v ON v.id_vuelo = iv.id_vuelo
            LEFT JOIN LATERAL (
                SELECT sv.id_aeropuerto_origen FROM segmento_vuelo sv WHERE sv.id_vuelo = v.id_vuelo ORDER BY sv.orden_segmento ASC LIMIT 1
            ) s1 ON true
            LEFT JOIN LATERAL (
                SELECT sv.id_aeropuerto_destino, sv.id_segmento FROM segmento_vuelo sv WHERE sv.id_vuelo = v.id_vuelo ORDER BY sv.orden_segmento DESC LIMIT 1
            ) s2 ON true
            LEFT JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen
            LEFT JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino
            LEFT JOIN asignacion_puerta ap ON ap.id_vuelo = v.id_vuelo
            LEFT JOIN puerta_embarque pe ON pe.id_puerta = ap.id_puerta
            LEFT JOIN reserva_asiento ra ON ra.id_reserva = :idReserva AND ra.id_vuelo = v.id_vuelo AND ra.rut = :rut
            LEFT JOIN asiento a ON a.id_asiento = ra.id_asiento
            WHERE ri.id_reserva = :idReserva
            ORDER BY CASE WHEN v.fecha_hora_salida >= :ahora THEN 0 ELSE 1 END, v.fecha_hora_salida ASC
            LIMIT 1
        """)
        .setParameter("idReserva", idReserva)
        .setParameter("rut", rutMatch)
        .setParameter("ahora", LocalDateTime.now(clock))
        .getResultList();

        String origIata = null, origCiudad = null, destIata = null, destCiudad = null;
        String numVuelo = null, asiento = null, puerta = null, terminal = null;
        LocalDateTime proximaSalida = null;

        if (!flightRows.isEmpty()) {
            Object[] f = flightRows.get(0);
            numVuelo = text(f[1]);
            proximaSalida = date(f[2]);
            origIata = text(f[3]);
            origCiudad = text(f[4]);
            destIata = text(f[5]);
            destCiudad = text(f[6]);
            puerta = text(f[7]);
            terminal = text(f[8]);
            asiento = text(f[9]);
        }

        String bcbp = null;
        String barcodeBase64 = null;
        if (checkinHecho && numVuelo != null && asiento != null) {
            bcbp = BoardingPassSecurityService.generarCodigoSeguro(pnrOficial, numVuelo, asiento, rutMatch);
            barcodeBase64 = BarcodeGeneratorUtil.generarPdf417DataUrl(bcbp);
        }

        return new ConsultaPnrResponse(
                idReserva, pnrOficial, estadoReserva, nombrePasajero,
                origIata, destIata, origCiudad, destCiudad,
                proximaSalida, numVuelo, asiento, puerta, terminal,
                checkinHecho, bcbp, bcbp, barcodeBase64
        );
    }
}
