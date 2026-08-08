package com.SkyWay.modules.puertaembarque.domain.repository;

import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuertaEmbarqueRepository extends JpaRepository<PuertaEmbarque, Integer> {
    // Ya tienes acceso a todos los métodos CRUD por defecto
    List<PuertaEmbarque> findByAeropuertoIdAeropuerto(Integer idAeropuerto);
}
