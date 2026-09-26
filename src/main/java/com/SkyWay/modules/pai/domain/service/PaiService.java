package com.SkyWay.modules.pai.domain.service;



import com.SkyWay.modules.pai.domain.model.Pai;

import java.util.List;
import java.util.Optional;

public interface PaiService {
    List<Pai> findAllOrdenados();
    Pai guardarPais(String nombre);
    Optional<Pai> buscarPorNombre(String nombre);
    Pai obtenerPorId(Integer idPais);
}