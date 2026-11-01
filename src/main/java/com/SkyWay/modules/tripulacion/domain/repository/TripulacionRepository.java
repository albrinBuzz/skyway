package com.SkyWay.modules.tripulacion.domain.repository;

import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripulacionRepository extends JpaRepository<Tripulacion, String> {

    Optional<Tripulacion> findByRut(String rut);

    // Find all Tripulaciones
    List<Tripulacion> findAll();

    // Find Tripulaciones by cargo
    List<Tripulacion> findByCargo(String cargo);

    // Check if Tripulacion exists by rut
    boolean existsByRut(String rut);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tripulacion (rut, cargo, fecha_ingreso) VALUES (:rut, :cargo, NOW()) ON CONFLICT (rut) DO UPDATE SET cargo = EXCLUDED.cargo", nativeQuery = true)
    void upsertTripulacion(@Param("rut") String rut, @Param("cargo") String cargo);
}
