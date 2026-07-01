package com.SkyWay.modules.estadovuelo.domain.service;

import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;

import java.util.List;
import java.util.Optional;



public interface EstadoVueloService {
    EstadoVuelo save(EstadoVuelo estadoVuelo);
    List<EstadoVuelo> findAll();
    Optional<EstadoVuelo> findById(Integer id);
    void deleteById(Integer id);
}
