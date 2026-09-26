package com.SkyWay.modules.pai.domain.repository;

import com.SkyWay.modules.pai.domain.model.Pai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaisRepository extends JpaRepository<Pai, Integer> {
    List<Pai> findAllByOrderByNombreAsc();
    Optional<Pai> findByNombreIgnoreCase(String nombre);
}