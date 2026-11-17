package com.SkyWay.modules.reembolso.domain.repository;

import com.SkyWay.modules.reembolso.domain.model.Reembolso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReembolsoRepository extends JpaRepository<Reembolso, Integer> {
    Optional<Reembolso> findByIdCancelacion(Integer idCancelacion);
}
