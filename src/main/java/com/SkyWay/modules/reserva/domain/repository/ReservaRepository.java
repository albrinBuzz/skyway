package com.SkyWay.modules.reserva.domain.repository;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer>{

	
	
}
