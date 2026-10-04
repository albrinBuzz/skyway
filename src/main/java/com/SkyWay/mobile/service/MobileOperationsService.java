package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.notificacion.domain.repository.NotificacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public MobileOperationsService(ReservaRepository reservas,NotificacionRepository notifications,MobileReadService read,MobileAuthService auth,Clock clock) {
        this.reservas=reservas; this.notifications=notifications; this.read=read; this.auth=auth; this.clock=clock;
    }
    private String reason(DetalleReserva detail,VueloDto flight) {
        if(!"Confirmada".equalsIgnoreCase(detail.estadoReserva().descripcion())) return "La reserva debe estar confirmada y sin check-in.";
        if(detail.checkin()!=null) return "El check-in ya fue registrado para esta reserva.";
        if(flight.asiento()==null) return "No hay un asiento previo del pasajero para cambiar.";
        if(!operable(flight)) return "El vuelo no está disponible para cambios.";
        LocalDateTime departure=flight.fechaHoraSalida();
        if(departure==null || !departure.isAfter(LocalDateTime.now(clock))) return "El vuelo ya salió o no tiene horario definido.";
        var fares=detail.itinerarios().stream().filter(i->i.vuelos().stream().anyMatch(v->v.vuelo().idVuelo().equals(flight.idVuelo()))).map(ItinerarioDto::tarifa).toList();
        if(fares.isEmpty()) return "La tarifa no está definida.";
        for(var fare:fares) {
            boolean allowed=fare.caracteristicas().stream().anyMatch(c->"Permite Cambios Asiento".equalsIgnoreCase(c.nombre())&&Boolean.TRUE.equals(c.valorBool()));
            if(!allowed) return "La tarifa "+fare.nombre()+" no autoriza cambios de asiento.";
            for(var c:fare.caracteristicas()) if("Horas Minimas Cambio Asiento".equalsIgnoreCase(c.nombre())) {
                if(c.valorInt()==null || c.valorInt()<0) return "La anticipación mínima de la tarifa requiere revisión.";
                if(LocalDateTime.now(clock).plusHours(c.valorInt()).isAfter(departure))
                    return "La tarifa exige al menos "+c.valorInt()+" horas antes de la salida.";
            }
        }
        return null;
    }
    private static boolean operable(VueloDto flight) {
        if(cancelado(flight)||flight.estado().idEstadoVuelo()==null) return false;
        String state=Objects.toString(flight.estado().estado(),"");
        String desc=Objects.toString(flight.estado().descripcion(),"");
        return !"Finalizado".equalsIgnoreCase(state)&&!"En vuelo".equalsIgnoreCase(desc)&&!"Aterrizado".equalsIgnoreCase(desc);
    }
    public MapaAsientos mapa(String rut,int id,int flightId) {
        var detail=read.detalle(rut,id); var flight=vuelo(detail,flightId); String reason=reason(detail,flight);
        var seats=reservas.mobileMapa(flightId,id,rut).stream().map(r->{
            boolean selectable=reason==null&&"libre".equals(r[5])&&r[4]!=null
                    &&Objects.equals(number(r[2]),flight.asiento().idClase());
            return new AsientoDto(number(r[0]),text(r[1]),number(r[2]),text(r[3]),number(r[4]),text(r[5]),selectable);
        }).toList();
        return new MapaAsientos(id,flightId,reason==null,reason,flight.asiento(),seats);
    }
    @Transactional
    public MapaAsientos cambiar(String rut,int id,int flightId,int seatId) {
        auth.requirePassenger(rut);
        reservas.mobileReserva(rut,id,true); // Mismo bloqueo que check-in: evita carreras entre ambas operaciones.
        // Autorizar el vuelo antes de bloquearlo.
        vuelo(read.detalle(rut,id),flightId);
        reservas.mobileLockVuelo(flightId);
        var map=mapa(rut,id,flightId);
        if(map.asientoActual()!=null&&map.asientoActual().idAsiento()==seatId) return map; // PUT idempotente.
        if(!map.cambioPermitido()) throw MobileException.conflict(map.motivo());
        var target=map.asientos().stream().filter(a->a.idAsiento()==seatId).findFirst().orElseThrow(MobileException::notFound);
        if(!target.seleccionable()) throw MobileException.conflict("Asiento ocupado, sin precio o de una clase distinta a la contratada.");
        if(reservas.mobileCambiarAsiento(map.asientoActual().idReservaAsiento(),id,flightId,rut,seatId)!=1)
            throw MobileException.conflict("La asignación cambió. Actualiza el mapa e inténtalo otra vez.");
        return mapa(rut,id,flightId);
    }
    @Transactional
    public CheckinDto checkin(String rut,int id) {
        auth.requirePassenger(rut); reservas.mobileReserva(rut,id,true);
        var detail=read.detalle(rut,id);
        if("Cancelada".equalsIgnoreCase(detail.estadoReserva().descripcion())) throw MobileException.conflict("La reserva está cancelada.");
        if(detail.checkin()!=null) return detail.checkin();
        if(!detail.titular()) throw new MobileException(HttpStatus.FORBIDDEN,"SOLO_TITULAR","El titular debe registrar el check-in de toda la reserva.");
        if(!"Confirmada".equalsIgnoreCase(detail.estadoReserva().descripcion())) throw MobileException.conflict("La reserva debe estar confirmada.");
        var flights=vuelos(detail);
        if(flights.isEmpty()) throw MobileException.conflict("La reserva no contiene vuelos.");
        // Orden estable de bloqueos, también frente al cambio de asientos.
        flights.stream().map(VueloDto::idVuelo).sorted().forEach(reservas::mobileLockVuelo);
        detail=read.detalle(rut,id); flights=vuelos(detail);
        var now=LocalDateTime.now(clock);
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
