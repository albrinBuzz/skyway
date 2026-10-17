package com.SkyWay.modules.metodopago.domain.service;

import com.SkyWay.modules.metodopago.domain.model.MetodoPago;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoService {

    MetodoPago save(MetodoPago metodoPago);

    Optional<MetodoPago> findById(Integer idMetodoPago);

    Optional<MetodoPago> findByDescripcion(String descripcion);

    List<MetodoPago> findAll();
}