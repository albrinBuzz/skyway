package com.SkyWay.modules.continente.domain.service;

import com.SkyWay.modules.continente.domain.model.Continente;

import java.util.List;
import java.util.Optional;

public interface ContinenteService {

    List<Continente> findAll();

    Optional<Continente> findById(Integer id);

    Optional<Continente> buscarPorNombre(String nombre);

    Continente guardar(Continente continente);

    void eliminar(Integer id);
}