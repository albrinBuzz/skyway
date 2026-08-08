package com.SkyWay.modules.tarifa.domain.service;

import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.domain.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    public List<Tarifa> listarTarifas() {
        return tarifaRepository.findAll();
    }

    public Tarifa guardarTarifa(Tarifa tarifa) {
        return tarifaRepository.save(tarifa);
    }

    public Optional<Tarifa> buscarPorId(Integer id) {
        return tarifaRepository.findById(id);
    }

    public void eliminarTarifa(Integer id) {
        tarifaRepository.deleteById(id);
    }
}
