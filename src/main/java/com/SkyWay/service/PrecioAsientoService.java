package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.PrecioAsiento;
import com.SkyWay.model.Vuelo;

public interface PrecioAsientoService {

    PrecioAsiento guardarPrecioAsiento(PrecioAsiento precioAsiento);
    
    Optional<PrecioAsiento> obtenerPrecioAsientoPorId(Integer id);

    List<PrecioAsiento> obtenerTodosLosPreciosAsiento();

    PrecioAsiento actualizarPrecioAsiento(Integer id, PrecioAsiento precioAsiento);

    void eliminarPrecioAsiento(Integer id);
    
    List<PrecioAsiento> findByVuelo(Vuelo vuelo);
}
