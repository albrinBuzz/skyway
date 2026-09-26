package com.SkyWay.modules.capacidadclase.domain.repository;

import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapacidadClaseRepository extends JpaRepository<CapacidadClase, Integer> {
    // JpaRepository ya trae métodos CRUD listos

    List<CapacidadClase> findByAvion1(Avion avion);

    // Alternativamente, si solo tienes el id del avion:
    List<CapacidadClase> findByAvion1_IdAvion(Integer idAvion);

    void deleteByAvion1_IdAvion(Integer idAvion);

}
