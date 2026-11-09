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

    List<Pai> findByActivoTrueOrderByNombreAsc();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE pais SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_pais = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE pais SET activo = true, fecha_baja = NULL WHERE id_pais = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}