package com.SkyWay.modules.reservaasiento.domain.repository;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface ReservaAsientoRepository extends JpaRepository<ReservaAsiento, Integer> {

	ReservaAsiento findByReserva(Reserva reserva);
	
}
