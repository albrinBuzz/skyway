package com.SkyWay.modules.segmentovuelo.domain.service;



import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;

import java.util.List;
import java.util.Optional;

public interface SegmentoVueloService {

    SegmentoVuelo save(SegmentoVuelo segmentoVuelo);

    SegmentoVuelo update(SegmentoVuelo segmentoVuelo);

    void deleteById(Integer id);

    Optional<SegmentoVuelo> findById(Integer id);

    List<SegmentoVuelo> findAll();
    // Método adicional opcional para buscar por vuelo
    List<SegmentoVuelo> findByIdVuelo(Integer idVuelo);

}
