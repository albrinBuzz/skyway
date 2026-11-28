package com.SkyWay.modules.aeropuerto.domain.service;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import com.SkyWay.modules.aeropuerto.presentation.dto.PaisDestinosDTO;

import java.util.List;
import java.util.Optional;

public interface AeropuertoService {

    List<Aeropuerto> findAll();
    Optional<Aeropuerto> findById(Integer id);
    Aeropuerto save(Aeropuerto aeropuerto);
    void delete(Integer id);
    void reactivar(Integer id);
    Aeropuerto update(Aeropuerto aeropuerto);
    List<AeropuertoMapaDTO> findAllParaMapa();
    List<AeropuertoMapaProjection> findAllConCoordenadas();
    List<AeropuertoMapaProjection> findAllConCoordenadasAdmin();

    // Métodos CRUD personalizados
    void registrarAeropuerto(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception;
    void registrarAeropuerto(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud, int cantidadPuertas, String terminal) throws Exception;
    void actualizarAeropuerto(Integer id, String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception;
    long contarPuertasActivasPorAeropuerto(Integer idAeropuerto);

    long contarAeropuertosActivos();
    long contarPaisesActivos();
    List<PaisDestinosDTO> obtenerDestinosAgrupadosPorPais();
}