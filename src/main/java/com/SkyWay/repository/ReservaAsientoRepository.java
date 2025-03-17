package com.SkyWay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.Reserva;
import com.SkyWay.model.ReservaAsiento;


@Repository
public interface ReservaAsientoRepository extends JpaRepository<ReservaAsiento, Integer> {

	ReservaAsiento findByReserva(Reserva reserva);
	
}
