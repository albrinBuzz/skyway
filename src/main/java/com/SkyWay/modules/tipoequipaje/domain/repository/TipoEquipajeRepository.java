package com.SkyWay.modules.tipoequipaje.domain.repository;

import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoEquipajeRepository extends JpaRepository<TipoEquipaje, Integer> {

    Optional<TipoEquipaje> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}