package com.SkyWay.modules.asignacionpuerta.domain.repository;


import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionPuertaRepository extends JpaRepository<AsignacionPuerta, Integer> {
    // Puedes agregar métodos personalizados aquí si lo necesitas
    List<AsignacionPuerta> findBySegmentoVuelo(SegmentoVuelo segmentoVuelo);
}
