package com.SkyWay.modules.puertaembarque.domain.repository;

import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuertaEmbarqueRepository extends JpaRepository<PuertaEmbarque, Integer> {
    // Ya tienes acceso a todos los métodos CRUD por defecto
    List<PuertaEmbarque> findByAeropuertoIdAeropuerto(Integer idAeropuerto);

    List<PuertaEmbarque> findByAeropuertoIdAeropuertoAndActivoTrue(Integer idAeropuerto);

    List<PuertaEmbarque> findByActivoTrue();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE puerta_embarque SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_puerta = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE puerta_embarque SET activo = true, fecha_baja = NULL WHERE id_puerta = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
}
