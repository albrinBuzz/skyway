package com.SkyWay.modules.claseasiento.domain.service;

import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;

import java.util.List;
import java.util.Optional;



public interface ClaseAsientoService {

    ClaseAsiento guardarClaseAsiento(ClaseAsiento claseAsiento);

    Optional<ClaseAsiento> obtenerClaseAsientoPorId(Integer id);

    List<ClaseAsiento> obtenerTodasLasClasesAsiento();

    ClaseAsiento actualizarClaseAsiento(Integer id, ClaseAsiento claseAsiento);

    void eliminarClaseAsiento(Integer id);
}
