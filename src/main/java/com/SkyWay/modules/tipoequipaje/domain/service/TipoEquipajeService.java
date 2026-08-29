package com.SkyWay.modules.tipoequipaje.domain.service;

import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;

import java.util.List;

public interface TipoEquipajeService {
    List<TipoEquipaje> listarTodos();
    TipoEquipaje buscarPorId(Integer id);
    TipoEquipaje guardar(TipoEquipaje tipoEquipaje);
    void eliminar(Integer id);
}