package com.SkyWay.service;


import java.util.List;
import java.util.Optional;

import com.SkyWay.model.Aeropuerto;

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
