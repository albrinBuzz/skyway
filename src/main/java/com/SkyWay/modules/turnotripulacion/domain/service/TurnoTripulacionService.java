package com.SkyWay.modules.turnotripulacion.domain.service;

import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.turnotripulacion.domain.repository.TurnoTripulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TurnoTripulacionService {

    @Autowired
    private TurnoTripulacionRepository turnoTripulacionRepository;

    // Save or update a TurnoTripulacion
    public TurnoTripulacion save(TurnoTripulacion turnoTripulacion) {
        return turnoTripulacionRepository.save(turnoTripulacion);
    }

    // Find all TurnoTripulacion records
    public List<TurnoTripulacion> findAll() {
        return turnoTripulacionRepository.findAll();
    }

    // Find a TurnoTripulacion by id
    public Optional<TurnoTripulacion> findById(Integer idTurnoTripulacion) {
        return turnoTripulacionRepository.findById(idTurnoTripulacion);
    }

    // Find TurnoTripulacion by Tripulacion's rut
    public List<TurnoTripulacion> findByTripulacionRut(String rut) {
        return turnoTripulacionRepository.findByTripulacion1Rut(rut);
    }

    // Find TurnoTripulacion by Turno's id
    public List<TurnoTripulacion> findByTurnoId(Integer idTurno) {
        return turnoTripulacionRepository.findByTurno1IdTurno(idTurno);
    }

    // Delete a TurnoTripulacion by id
    public void delete(Integer idTurnoTripulacion) {
        turnoTripulacionRepository.deleteById(idTurnoTripulacion);
    }
}

