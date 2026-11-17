package com.SkyWay.modules.reembolso.domain.repository;

import com.SkyWay.modules.reembolso.domain.model.CuentaReembolso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaReembolsoRepository extends JpaRepository<CuentaReembolso, Integer> {
    List<CuentaReembolso> findByRutPasajeroIgnoreCaseAndActivoTrueOrderByFechaRegistroDesc(String rutPasajero);
}
