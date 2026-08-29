package com.SkyWay.modules.equipaje.domain.service;


import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.presentation.dto.EquipajeDTO;

import java.math.BigDecimal;
import java.util.List;

public interface EquipajeService {
    List<Equipaje> listarTodos();
    Equipaje buscarPorId(Integer id);
    List<Equipaje> buscarPorReserva(Integer idReserva);
    List<Equipaje> buscarPorPasajero(String rut);
    Equipaje guardar(Equipaje equipaje, Integer idReserva, String rutPasajero, Integer idTipoEquipaje);
    void eliminar(Integer id);
    BigDecimal calcularPesoTotalPasajeroEnReserva(Integer idReserva, String rut);
}