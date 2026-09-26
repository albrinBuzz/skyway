package com.SkyWay.modules.modeloavion.domain.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModeloAvionRepository extends JpaRepository<ModeloAvion, Integer> {
    List<ModeloAvion> findAllByOrderByNombreAsc();
}