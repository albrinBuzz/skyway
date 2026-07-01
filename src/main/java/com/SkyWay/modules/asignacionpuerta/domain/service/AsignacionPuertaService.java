package com.SkyWay.modules.asignacionpuerta.domain.service;


import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;

import java.util.List;
import java.util.Optional;

public interface AsignacionPuertaService {
    AsignacionPuerta save(AsignacionPuerta asignacionPuerta);

    AsignacionPuerta update(AsignacionPuerta asignacionPuerta);

    void deleteById(Integer id);

    Optional<AsignacionPuerta> findById(Integer id);

    List<AsignacionPuerta> findAll();

    List<AsignacionPuerta> findBySegmentoVuelo(SegmentoVuelo segmentoVuelo);

}
