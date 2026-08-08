package com.SkyWay.modules.capacidadclase.domain.service;

import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.capacidadclase.domain.repository.CapacidadClaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CapacidadClaseService {

    @Autowired
    private CapacidadClaseRepository repository;

    public List<CapacidadClase> findAll() {
        return repository.findAll();
    }

    public Optional<CapacidadClase> findById(Integer id) {
        return repository.findById(id);
    }

    public CapacidadClase save(CapacidadClase capacidadClase) {
        return repository.save(capacidadClase);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<CapacidadClase> findByAvion(Avion avion) {
        return repository.findByAvion1(avion);
    }

    public List<CapacidadClase> findByAvionId(Integer idAvion) {
        return repository.findByAvion1_IdAvion(idAvion);
    }
}
