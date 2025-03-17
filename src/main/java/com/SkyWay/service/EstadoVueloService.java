package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.EstadoVuelo;

public interface EstadoVueloService {
    EstadoVuelo save(EstadoVuelo estadoVuelo);
    List<EstadoVuelo> findAll();
    Optional<EstadoVuelo> findById(Integer id);
    void deleteById(Integer id);
}
