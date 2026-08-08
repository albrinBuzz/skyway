package com.SkyWay.modules.PasajeroReserva.domain.repository;


import com.SkyWay.modules.PasajeroReserva.domain.model.PasajeroReserva;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasajeroReservaRepository extends JpaRepository<PasajeroReserva, Long> {

    // Obtener todos los pasajeros de una reserva
    List<PasajeroReserva> findByReserva(Reserva reserva);

    // Buscar un pasajero específico en una reserva
    Optional<PasajeroReserva> findByReservaAndPasajero(Reserva reserva, Pasajero pasajero);

    // Eliminar todos los pasajeros de una reserva
    void deleteByReserva(Reserva reserva);
}
