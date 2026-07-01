package com.SkyWay.modules.notificacion.domain.service;

import com.SkyWay.modules.notificacion.domain.model.Notificacion;

import java.util.List;
import java.util.Optional;



public interface NotificacionService {

	Notificacion crearNotificacion(Notificacion notificacion);

    Optional<Notificacion> obtenerNotificacionPorId(Integer idNotificacion);

    List<Notificacion> obtenerTodasLasNotificaciones();

    //List<Notificacion> findByRut(String rut);
    
    void eliminarNotificacion(Integer idNotificacion);

    Notificacion marcarComoLeida(Integer idNotificacion);
}
