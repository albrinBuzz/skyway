package com.SkyWay.modules.reservaasiento.domain.service;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;

import java.util.List;
import java.util.Optional;



public interface ReservaAsientoService {



    // Crear o actualizar una reserva
    public ReservaAsiento guardarReserva(ReservaAsiento reservaAsiento);

    // Obtener todas las reservas
    public List<ReservaAsiento> obtenerTodasLasReservas();

    // Obtener una reserva por ID
    public Optional<ReservaAsiento> obtenerReservaPorId(Integer id);

    // Eliminar una reserva por ID
    public void eliminarReserva(Integer id);
    
    public boolean cambiarAsiento(Integer idAsiento,Integer idReserva,Integer id_asiento_org);
    
    ReservaAsiento findByReserva(Reserva reserva);
    
    
}
