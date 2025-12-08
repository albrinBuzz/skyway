package com.SkyWay.modules.tripulacion.domain.service;




import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.tripulacion.domain.repository.TripulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TripulacionService {

    private final TripulacionRepository tripulacionRepository;

    @Autowired
    public TripulacionService(TripulacionRepository tripulacionRepository) {
        this.tripulacionRepository = tripulacionRepository;
    }

    // Save a new Tripulacion
    public Tripulacion save(Tripulacion tripulacion) {
        return tripulacionRepository.save(tripulacion);
    }

    // Find a Tripulacion by rut
    public Optional<Tripulacion> findByRut(String rut) {
        return tripulacionRepository.findByRut(rut);
    }

    // Find all Tripulaciones
    public List<Tripulacion> findAll() {
        return tripulacionRepository.findAll();
    }

    // Update a Tripulacion
    public Tripulacion update(String rut, Tripulacion updatedTripulacion) {
        Optional<Tripulacion> existingTripulacion = findByRut(rut);
        if (existingTripulacion.isPresent()) {
            updatedTripulacion.setRut(rut);
            return tripulacionRepository.save(updatedTripulacion);
        } else {
            throw new RuntimeException("Tripulacion with rut " + rut + " not found");
        }
    }

    // Delete a Tripulacion by rut
    public void delete(String rut) {
        Optional<Tripulacion> tripulacion = findByRut(rut);
        if (tripulacion.isPresent()) {
            tripulacionRepository.deleteById(rut);
        } else {
            throw new RuntimeException("Tripulacion with rut " + rut + " not found");
        }
    }

    // Check if Tripulacion exists by rut
    public boolean existsByRut(String rut) {
        return tripulacionRepository.existsByRut(rut);
    }
}
