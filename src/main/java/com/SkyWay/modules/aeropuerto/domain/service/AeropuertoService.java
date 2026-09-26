package com.SkyWay.modules.aeropuerto.domain.service;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;

import java.util.List;
import java.util.Optional;

public interface AeropuertoService {

    List<Aeropuerto> findAll();
    Optional<Aeropuerto> findById(Integer id);
    Aeropuerto save(Aeropuerto aeropuerto);
    void delete(Integer id);
    Aeropuerto update(Aeropuerto aeropuerto);
    List<AeropuertoMapaDTO> findAllParaMapa();
    List<AeropuertoMapaProjection> findAllConCoordenadas();

    // Métodos CRUD personalizados
    void registrarAeropuerto(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception;
    void actualizarAeropuerto(Integer id, String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception;
}