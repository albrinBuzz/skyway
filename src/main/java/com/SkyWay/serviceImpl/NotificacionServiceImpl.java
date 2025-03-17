package com.SkyWay.serviceImpl;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.Notificacion;
import com.SkyWay.repository.NotificacionRepository;
import com.SkyWay.service.NotificacionService;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Autowired
    public NotificacionServiceImpl(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public Notificacion crearNotificacion(Notificacion notificacion) {
        return notificacionRepository.save(notificacion); // Guarda o actualiza la notificación
    }

    @Override
    public Optional<Notificacion> obtenerNotificacionPorId(Integer idNotificacion) {
        return notificacionRepository.findById(idNotificacion); // Encuentra la notificación por su ID
    }

    @Override
    public List<Notificacion> obtenerTodasLasNotificaciones() {
        return notificacionRepository.findAll(); // Obtiene todas las notificaciones
    }

    @Override
    public void eliminarNotificacion(Integer idNotificacion) {
        notificacionRepository.deleteById(idNotificacion); // Elimina la notificación por su ID
    }

    @Override
    public Notificacion marcarComoLeida(Integer idNotificacion) {
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(idNotificacion);
        if (notificacionOpt.isPresent()) {
            Notificacion notificacion = notificacionOpt.get();
            notificacion.setLeida(true); // Marca la notificación como leída
            return notificacionRepository.save(notificacion); // Guarda la notificación con el nuevo estado
        }
        return null; // Si no se encuentra la notificación, retorna null
    }
    
    @Override
	public List<Notificacion> findByRut(String rut){
    	return notificacionRepository.findByRut(rut);
    }
}