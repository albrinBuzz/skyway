package com.SkyWay.modules.modeloavion.domain.service;

import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModeloAvionService {

    @Autowired
    private ModeloAvionRepository modeloAvionRepository;

    @Transactional(readOnly = true)
    public List<ModeloAvion> findAll() {
        return modeloAvionRepository.findAll();
    }

    @Transactional
    public ModeloAvion guardar(ModeloAvion modelo) {
        if (modelo.getNombre() == null || modelo.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del modelo es obligatorio.");
        }
        if (modelo.getFabricante() == null) {
            throw new IllegalArgumentException("Debe asociar un fabricante al modelo.");
        }
        return modeloAvionRepository.save(modelo);
    }

    @Transactional
    public void eliminar(Integer id) {
        modeloAvionRepository.deleteById(id);
    }
}