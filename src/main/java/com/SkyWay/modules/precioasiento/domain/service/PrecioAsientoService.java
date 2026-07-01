package com.SkyWay.modules.precioasiento.domain.service;

import com.SkyWay.modules.precioasiento.domain.model.PrecioAsiento;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;

import java.util.List;
import java.util.Optional;


public interface PrecioAsientoService {

    PrecioAsiento guardarPrecioAsiento(PrecioAsiento precioAsiento);

    Optional<PrecioAsiento> obtenerPrecioAsientoPorId(Integer id);

    List<PrecioAsiento> obtenerTodosLosPreciosAsiento();

    PrecioAsiento actualizarPrecioAsiento(Integer id, PrecioAsiento precioAsiento);

    void eliminarPrecioAsiento(Integer id);

    List<PrecioAsiento> findByVuelo(Vuelo vuelo);

    // Nuevo método para evitar duplicados al actualizar
    Optional<PrecioAsiento> findByVueloAndClase(Integer idVuelo, Integer idClase);
}
