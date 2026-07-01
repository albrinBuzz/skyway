package com.SkyWay.modules.aeropuerto.domain.service;


import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;

import java.util.List;
import java.util.Optional;



public interface AeropuertoService {

    // Obtener todos los aeropuertos
    List<Aeropuerto> findAll();

    // Obtener un aeropuerto por ID
    Optional<Aeropuerto> findById(Integer id);

    // Crear un nuevo aeropuerto
    Aeropuerto save(Aeropuerto aeropuerto);

    // Eliminar un aeropuerto
    void delete(Integer id);

    // Actualizar un aeropuerto existente
    Aeropuerto update(Aeropuerto aeropuerto);
}
