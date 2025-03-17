package com.SkyWay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.ClaseAsiento;

@Repository
public interface ClaseAsientoRepository extends JpaRepository<ClaseAsiento, Integer> {
    // Aquí se pueden agregar consultas personalizadas si es necesario.
}
