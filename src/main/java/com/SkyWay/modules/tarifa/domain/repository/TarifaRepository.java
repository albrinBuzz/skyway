package com.SkyWay.modules.tarifa.domain.repository;


import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    // Puedes definir métodos adicionales si los necesitas, por ejemplo:
    Tarifa findByNombre(String nombre);

    Optional<Tarifa> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdTarifaNot(String nombre, Integer idTarifa);

    java.util.List<Tarifa> findByActivoTrue();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM tarifa ORDER BY id_tarifa ASC", nativeQuery = true)
    java.util.List<Tarifa> findAllAdmin();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE tarifa SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_tarifa = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE tarifa SET activo = true, fecha_baja = NULL WHERE id_tarifa = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}
