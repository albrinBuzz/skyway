package com.SkyWay.modules.claseasiento.domain.repository;

import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ClaseAsientoRepository extends JpaRepository<ClaseAsiento, Integer> {
    // Aquí se pueden agregar consultas personalizadas si es necesario.
    List<ClaseAsiento> findAllByOrderByDescripcionAsc();
}
