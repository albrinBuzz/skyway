package com.SkyWay.modules.pago.domain.service;

import com.SkyWay.modules.pago.domain.model.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoService {

    Pago save(Pago pago);

    Optional<Pago> findById(Integer idPago);

    List<Pago> findByReservaId(Integer idReserva);

    List<Pago> findAll();
}