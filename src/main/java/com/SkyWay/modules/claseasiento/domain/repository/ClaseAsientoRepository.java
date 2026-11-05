package com.SkyWay.modules.claseasiento.domain.repository;

import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ClaseAsientoRepository extends JpaRepository<ClaseAsiento, Integer> {
    // Aquí se pueden agregar consultas personalizadas si es necesario.
    List<ClaseAsiento> findAllByOrderByDescripcionAsc();

    List<ClaseAsiento> findByActivoTrueOrderByDescripcionAsc();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM clase_asiento ORDER BY id_clase ASC", nativeQuery = true)
    List<ClaseAsiento> findAllAdmin();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE clase_asiento SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_clase = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE clase_asiento SET activo = true, fecha_baja = NULL WHERE id_clase = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}
