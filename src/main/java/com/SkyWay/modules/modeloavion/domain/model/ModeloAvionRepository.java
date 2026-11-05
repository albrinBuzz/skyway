package com.SkyWay.modules.modeloavion.domain.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModeloAvionRepository extends JpaRepository<ModeloAvion, Integer> {
    List<ModeloAvion> findAllByOrderByNombreAsc();

    List<ModeloAvion> findByActivoTrueOrderByNombreAsc();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM modelo_avion ORDER BY nombre ASC", nativeQuery = true)
    List<ModeloAvion> findAllAdmin();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE modelo_avion SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_modelo = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE modelo_avion SET activo = true, fecha_baja = NULL WHERE id_modelo = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}