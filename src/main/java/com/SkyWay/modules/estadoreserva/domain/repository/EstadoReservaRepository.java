package com.SkyWay.modules.estadoreserva.domain.repository;


import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoReservaRepository extends JpaRepository<EstadoReserva, Integer> {
    Optional<EstadoReserva> findFirstByDescripcionIgnoreCase(String descripcion);
}
