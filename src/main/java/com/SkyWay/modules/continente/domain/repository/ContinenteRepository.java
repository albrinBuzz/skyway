package com.SkyWay.modules.continente.domain.repository;

import com.SkyWay.modules.continente.domain.model.Continente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContinenteRepository extends JpaRepository<Continente, Integer> {

    List<Continente> findAllByOrderByNombreAsc();

    Optional<Continente> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Continente> findByActivoTrueOrderByNombreAsc();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE continente SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_continente = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE continente SET activo = true, fecha_baja = NULL WHERE id_continente = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}