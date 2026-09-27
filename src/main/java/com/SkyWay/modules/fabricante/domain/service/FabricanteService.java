package com.SkyWay.modules.fabricante.domain.service;

import com.SkyWay.modules.fabricante.domain.model.Fabricante;

import com.SkyWay.modules.fabricante.domain.repository.FabricanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FabricanteService {

    @Autowired
    private FabricanteRepository fabricanteRepository;

    @Transactional(readOnly = true)
    public List<Fabricante> findAll() {
        return fabricanteRepository.findAll();
    }

    @Transactional
    public Fabricante guardar(Fabricante fabricante) {
        if (fabricante.getNombre() == null || fabricante.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del fabricante es obligatorio.");
        }
        return fabricanteRepository.save(fabricante);
    }

    @Transactional
    public void eliminar(Integer id) {
        fabricanteRepository.deleteById(id);
    }
}