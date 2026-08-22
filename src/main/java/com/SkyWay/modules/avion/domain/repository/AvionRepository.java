package com.SkyWay.modules.avion.domain.repository;

import com.SkyWay.modules.avion.domain.model.Avion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface AvionRepository extends JpaRepository<Avion, Integer> {

    @Query("SELECT DISTINCT a FROM Avion a " +
            "LEFT JOIN FETCH a.modeloAvion m " +
            "LEFT JOIN FETCH m.fabricante")
    List<Avion> findAllOptimized();
}