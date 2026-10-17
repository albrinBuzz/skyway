package com.SkyWay.modules.metodopago.domain.repository;

import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    Optional<MetodoPago> findByDescripcionIgnoreCase(String descripcion);
}