package com.SkyWay.modules.tipoequipaje.domain.repository;

import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoEquipajeRepository extends JpaRepository<TipoEquipaje, Integer> {

    Optional<TipoEquipaje> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    java.util.List<TipoEquipaje> findByActivoTrue();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM tipo_equipaje ORDER BY id_tipo ASC", nativeQuery = true)
    java.util.List<TipoEquipaje> findAllAdmin();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE tipo_equipaje SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_tipo = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE tipo_equipaje SET activo = true, fecha_baja = NULL WHERE id_tipo = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}