package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.Notificacion;

public interface NotificacionService {

	Notificacion crearNotificacion(Notificacion notificacion);

    Optional<Notificacion> obtenerNotificacionPorId(Integer idNotificacion);

    List<Notificacion> obtenerTodasLasNotificaciones();

    List<Notificacion> findByRut(String rut);
    
    void eliminarNotificacion(Integer idNotificacion);

    Notificacion marcarComoLeida(Integer idNotificacion);
}
