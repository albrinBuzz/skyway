package com.SkyWay.modules.turno.domain.repository;

import com.SkyWay.modules.turno.domain.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Integer> {

    // Find all Turnos
    List<Turno> findAll();

    // Find Turno by ID
    Optional<Turno> findByIdTurno(Integer idTurno);

    // Find Turnos by Vuelo's ID
    List<Turno> findByVueloIdVuelo(Integer idVuelo);

    // Find Turnos by TipoTurno's ID
    //List<Turno> findByTipoTurnoIdTipoTurno(Integer idTipoTurno);
}
