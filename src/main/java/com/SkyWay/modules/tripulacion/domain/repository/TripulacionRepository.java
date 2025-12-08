package com.SkyWay.modules.tripulacion.domain.repository;




import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
