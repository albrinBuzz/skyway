package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.ClaseAsiento;

public interface ClaseAsientoService {

    ClaseAsiento guardarClaseAsiento(ClaseAsiento claseAsiento);

    Optional<ClaseAsiento> obtenerClaseAsientoPorId(Integer id);

    List<ClaseAsiento> obtenerTodasLasClasesAsiento();

    ClaseAsiento actualizarClaseAsiento(Integer id, ClaseAsiento claseAsiento);

    void eliminarClaseAsiento(Integer id);
}
