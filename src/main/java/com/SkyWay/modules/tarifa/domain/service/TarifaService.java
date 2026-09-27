package com.SkyWay.modules.tarifa.domain.service;

import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.domain.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Transactional(readOnly = true)
    public List<Tarifa> findAll() {
        return tarifaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Tarifa> findById(Integer id) {
        return tarifaRepository.findById(id);
    }

    @Transactional
    public Tarifa save(Tarifa tarifa) {
        return tarifaRepository.save(tarifa);
    }

    @Transactional
    public void deleteById(Integer id) {
        tarifaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByNombreAndNotId(String nombre, Integer id) {
        if (id == null) {
            return tarifaRepository.findByNombreIgnoreCase(nombre).isPresent();
        }
        return tarifaRepository.existsByNombreIgnoreCaseAndIdTarifaNot(nombre, id);
    }
}