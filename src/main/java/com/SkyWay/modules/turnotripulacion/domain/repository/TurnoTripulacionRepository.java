package com.SkyWay.modules.turnotripulacion.domain.repository;


import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoTripulacionRepository extends JpaRepository<TurnoTripulacion, Integer> {

    Optional<TurnoTripulacion> findByIdTurnoTripulacion(Integer idTurnoTripulacion);

    // Find TurnoTripulacion by Tripulacion's rut
    List<TurnoTripulacion> findByTripulacion1Rut(String rut);

    // Find TurnoTripulacion by Turno's id
    List<TurnoTripulacion> findByTurno1IdTurno(Integer idTurno);
}
