package com.SkyWay.modules.tarifaItinerario.domain.service;


import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.repository.ItinerarioTarifaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItinerarioTarifaService {

    private final ItinerarioTarifaRepository repository;

    public ItinerarioTarifaService(ItinerarioTarifaRepository repository) {
        this.repository = repository;
    }

    public List<ItinerarioTarifa> findAll() {
        return repository.findAll();
    }

    public Optional<ItinerarioTarifa> findById(Integer id) {
        return repository.findById(id);
    }

    public ItinerarioTarifa save(ItinerarioTarifa entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<ItinerarioTarifa> findByItinerario(Integer idItinerario) {
        return repository.findByItinerarioIdItinerario(idItinerario);
    }
    public ItinerarioTarifa getByTarifaAndItinerario(Integer idItinerario, Integer idTarifa){
        return repository.getByTarifaAndItinerario(idItinerario,idTarifa);
    }

}
