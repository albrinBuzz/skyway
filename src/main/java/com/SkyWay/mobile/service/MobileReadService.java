package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.notificacion.domain.repository.NotificacionRepository;
import com.SkyWay.modules.notificacion.domain.model.Notificacion;
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

    public MobileReadService(ReservaRepository reservas, NotificacionRepository notificaciones, MobileAuthService auth, Clock clock) {
        this.reservas=reservas; this.notificaciones=notificaciones; this.auth=auth; this.clock=clock;
    }
    public static Integer number(Object o) { return o == null ? null : ((Number)o).intValue(); }
    public static String text(Object o) { return o == null ? null : o.toString(); }
    public static LocalDateTime date(Object o) {
        if (o == null) return null;
        return o instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime)o;
    }
    private static AeropuertoDto airport(Object[] r,int start) {
        return r[start] == null ? null : new AeropuertoDto(number(r[start]),text(r[start+1]),text(r[start+2]));
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
        var data=reservas.mobileReservas(rut,active,now,size,page*size).stream().map(r -> new ResumenReserva(
                number(r[0]),date(r[1]),new EstadoReservaDto(number(r[2]),text(r[3])),(BigDecimal)r[4],active,
                date(r[5]),text(r[6]),text(r[7]))).toList();
        return Pagina.of(data,page,size,reservas.mobileCountReservas(rut,active,now));
    }
    public DetalleReserva detalle(String rut,int id) {
        auth.requirePassenger(rut);
        Object[] booking=reservas.mobileReserva(rut,id,false);
        Map<Integer,List<SegmentoDto>> segments=new HashMap<>();
        for(Object[] r:reservas.mobileSegmentos(id)) {
            PuertaDto gate=r[11]==null?null:new PuertaDto(number(r[11]),text(r[12]),text(r[13]),number(r[14]));
            segments.computeIfAbsent(number(r[0]),k->new ArrayList<>()).add(new SegmentoDto(
                    number(r[1]),number(r[2]),date(r[3]),date(r[4]),airport(r,5),airport(r,8),gate));
        }
        Map<Integer,AsientoAsignado> seats=new HashMap<>();
        for(Object[] r:reservas.mobileAsignados(id,rut)) {
            if(seats.put(number(r[0]),new AsientoAsignado(number(r[1]),number(r[2]),text(r[3]),number(r[4]),text(r[5])))!=null)
                throw MobileException.conflict("La reserva tiene más de un asiento para el mismo pasajero y vuelo. Solicita su revisión.");
        }
        Map<Integer,List<ConexionDto>> connections=new HashMap<>();
        for(Object[] r:reservas.mobileConexiones(id)) {
            int flight=number(r[5]);
            VueloDto v=new VueloDto(flight,text(r[6]),date(r[7]),date(r[8]),number(r[9]),
                    new EstadoVueloDto(number(r[10]),text(r[11]),text(r[12])),segments.getOrDefault(flight,List.of()),seats.get(flight));
            connections.computeIfAbsent(number(r[0]),k->new ArrayList<>()).add(new ConexionDto(number(r[1]),number(r[2]),text(r[3]),text(r[4]),v));
        }
        Map<Integer,List<CaracteristicaDto>> features=new HashMap<>();
        for(Object[] r:reservas.mobileCaracteristicas(id)) features.computeIfAbsent(number(r[0]),k->new ArrayList<>()).add(
                new CaracteristicaDto(number(r[1]),number(r[2]),text(r[3]),text(r[4]),text(r[5]),text(r[6]),(Boolean)r[7],number(r[8])));
        var itineraries=reservas.mobileItinerarios(id).stream().map(r->new ItinerarioDto(
                number(r[0]),number(r[1]),date(r[2]),date(r[3]),number(r[4]),number(r[5]),airport(r,6),airport(r,9),
                new TarifaDto(number(r[12]),number(r[13]),text(r[14]),(BigDecimal)r[15],features.getOrDefault(number(r[13]),List.of())),
                connections.getOrDefault(number(r[1]),List.of()))).toList();
        var bags=reservas.mobileEquipajes(id,rut).stream().map(r->new EquipajeDto(number(r[0]),(BigDecimal)r[1],text(r[2]),text(r[3]),number(r[4]),text(r[5]))).toList();
        return new DetalleReserva(number(booking[0]),date(booking[1]),new EstadoReservaDto(number(booking[2]),text(booking[3])),
                (BigDecimal)booking[4],rut.equals(booking[5]),itineraries,bags,checkin(id));
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
        if (detail.checkin() == null) throw MobileException.conflict("Primero debes completar el check-in.");

        var flight = vuelo(detail, flightId);
        if (flight.asiento() == null) throw MobileException.conflict("No tienes asiento asignado.");

        // String codificado formateado para el QR de prueba
        String qrContenido = String.format("SKYWAY|CH:%d|RES:%d|FL:%d|SEAT:%s|RUT:%s",
                detail.checkin().idCheckin(),
                idReserva,
                flightId,
                flight.asiento().numeroAsiento(),
                rut
        );

        return new PaseAbordar(detail.checkin(), auth.perfil(rut).usuario(), flight, qrContenido, true);
    }

    public static boolean cancelado(VueloDto v) {
        String status=Objects.toString(v.estado().estado(),"").toLowerCase(Locale.ROOT);
        String desc=Objects.toString(v.estado().descripcion(),"").toLowerCase(Locale.ROOT);
        return List.of("cancelado","inactivo").contains(status)||List.of("cancelado","vuelo cancelado").contains(desc);
    }
}
