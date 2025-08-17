package com.SkyWay.modules.itinerariovuelo.domain.service;


import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.itinerariovuelo.domain.repository.ItinerarioVueloRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItinerarioVueloService {

    private final ItinerarioVueloRepository repository;

    public ItinerarioVueloService(ItinerarioVueloRepository repository) {
        this.repository = repository;
    }

    // Create or Update
    public ItinerarioVuelo save(ItinerarioVuelo itinerarioVuelo) {
        return repository.save(itinerarioVuelo);
    }

    // Read - find all
    public List<ItinerarioVuelo> findAll() {
        return repository.findAll();
    }

    // Read - find by ID
    public Optional<ItinerarioVuelo> findById(Integer id) {
        return repository.findById(id);
    }

    // Delete by ID
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    // Custom: find by itinerario ID
    public List<ItinerarioVuelo> findByItinerarioId(Integer idItinerario) {
        return repository.findByItinerarioIdItinerario(idItinerario);
    }

    // Custom: find by tipoConexion
    public List<ItinerarioVuelo> findByTipoConexion(String tipoConexion) {
        return repository.findByTipoConexion(tipoConexion);
    }
}
