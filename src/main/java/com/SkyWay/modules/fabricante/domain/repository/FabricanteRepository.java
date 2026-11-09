package com.SkyWay.modules.fabricante.domain.repository;

import com.SkyWay.modules.fabricante.domain.model.Fabricante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FabricanteRepository extends JpaRepository<Fabricante, Integer> {

    /**
     * Busca un fabricante por su nombre exacto ignora mayúsculas/minúsculas.
     */
    Optional<Fabricante> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe un fabricante con el mismo nombre.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    java.util.List<Fabricante> findByActivoTrue();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM fabricante ORDER BY nombre ASC", nativeQuery = true)
    java.util.List<Fabricante> findAllAdmin();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE fabricante SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_fabricante = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE fabricante SET activo = true, fecha_baja = NULL WHERE id_fabricante = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}