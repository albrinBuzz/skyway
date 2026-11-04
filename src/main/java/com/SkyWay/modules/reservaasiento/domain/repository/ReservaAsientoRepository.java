package com.SkyWay.modules.reservaasiento.domain.repository;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaAsientoRepository extends JpaRepository<ReservaAsiento, Integer> {

	ReservaAsiento findByReserva(Reserva reserva);

	@Query("SELECT ra FROM ReservaAsiento ra " +
		   "LEFT JOIN FETCH ra.pasajero p " +
		   "LEFT JOIN FETCH p.usuario u " +
		   "LEFT JOIN FETCH ra.asiento a " +
		   "WHERE ra.reserva.idReserva = :idReserva AND ra.vuelo.idVuelo = :idVuelo")
	List<ReservaAsiento> findAsignacionesByReservaYVuelo(@Param("idReserva") Integer idReserva, @Param("idVuelo") Integer idVuelo);
}
