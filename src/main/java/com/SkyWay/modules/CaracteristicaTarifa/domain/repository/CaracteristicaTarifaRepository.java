package com.SkyWay.modules.CaracteristicaTarifa.domain.repository;

import com.SkyWay.modules.CaracteristicaTarifa.domain.model.CaracteristicaTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaracteristicaTarifaRepository extends JpaRepository<CaracteristicaTarifa, Integer> {

    java.util.List<CaracteristicaTarifa> findByActivoTrue();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM caracteristica_tarifa ORDER BY id_caracteristica ASC", nativeQuery = true)
    java.util.List<CaracteristicaTarifa> findAllAdmin();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE caracteristica_tarifa SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_caracteristica = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE caracteristica_tarifa SET activo = true, fecha_baja = NULL WHERE id_caracteristica = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}