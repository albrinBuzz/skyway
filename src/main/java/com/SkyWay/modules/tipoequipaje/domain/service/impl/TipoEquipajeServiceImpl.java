package com.SkyWay.modules.tipoequipaje.service.impl;

import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import com.SkyWay.modules.tipoequipaje.domain.repository.TipoEquipajeRepository;

import com.SkyWay.modules.tipoequipaje.domain.service.TipoEquipajeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoEquipajeServiceImpl implements TipoEquipajeService {

    private final TipoEquipajeRepository tipoEquipajeRepository;

    public TipoEquipajeServiceImpl(TipoEquipajeRepository tipoEquipajeRepository) {
        this.tipoEquipajeRepository = tipoEquipajeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEquipaje> listarTodos() {
        return tipoEquipajeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoEquipaje buscarPorId(Integer id) {
        return tipoEquipajeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de Equipaje no encontrado con ID: " + id));
    }

    @Override
    public TipoEquipaje guardar(TipoEquipaje tipoEquipaje) {
        if (tipoEquipaje.getIdTipo() == null && tipoEquipajeRepository.existsByNombreIgnoreCase(tipoEquipaje.getNombre())) {
            throw new IllegalArgumentException("Ya existe un tipo de equipaje registrado con el nombre: " + tipoEquipaje.getNombre());
        }
        return tipoEquipajeRepository.save(tipoEquipaje);
    }

    @Override
    public void eliminar(Integer id) {
        if (!tipoEquipajeRepository.existsById(id)) {
            throw new RuntimeException("No existe el registro de Tipo de Equipaje a eliminar.");
        }
        tipoEquipajeRepository.deleteById(id);
    }
}