package com.SkyWay.modules.reserva.domain.repository;

import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import org.jboss.logging.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer>{


    // Busca todas las reservas por pasajero
    List<Reserva> findByPasajero(Pasajero pasajero);


    Page<Reserva> findByPasajero_Rut(String rut, Pageable pageable);


    // Opcional: buscar por el RUT del pasajero directamente (si el RUT es String o Integer)
    List<Reserva> findByPasajero_Rut(String rut);
	
}
