package com.SkyWay.modules.pago.domain.repository;

import com.SkyWay.modules.pago.domain.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // Buscar historial de pagos asociados a una reserva
    List<Pago> findByReserva_IdReserva(Integer idReserva);
}