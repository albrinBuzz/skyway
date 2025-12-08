package com.SkyWay.modules.turno.domain.service;

import com.SkyWay.modules.turno.domain.model.Turno;
import com.SkyWay.modules.turno.domain.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    // Save or update a Turno
    public Turno save(Turno turno) {
        return turnoRepository.save(turno);
    }

    // Find all Turnos
    public List<Turno> findAll() {
        return turnoRepository.findAll();
    }

    // Find a Turno by ID
    public Optional<Turno> findById(Integer idTurno) {
        return turnoRepository.findByIdTurno(idTurno);
    }

    // Find Turnos by Vuelo's ID
    public List<Turno> findByVueloId(Integer idVuelo) {
        return turnoRepository.findByVueloIdVuelo(idVuelo);
    }

    // Find Turnos by TipoTurno's ID
    /*public List<Turno> findByTipoTurnoId(Integer idTipoTurno) {
        return turnoRepository.findByTipoTurnoIdTipoTurno(idTipoTurno);
    }*/

    // Delete a Turno by ID
    public void delete(Integer idTurno) {
        turnoRepository.deleteById(idTurno);
    }
}
