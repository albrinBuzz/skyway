package com.SkyWay.modules.avion.domain.repository;

import com.SkyWay.modules.avion.domain.model.Avion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


public interface AvionRepository extends JpaRepository<Avion, Integer> {

    @Query("SELECT DISTINCT a FROM Avion a " +
            "LEFT JOIN FETCH a.modeloAvion m " +
            "LEFT JOIN FETCH m.fabricante " +
            "WHERE UPPER(a.estadoDeMantenimiento) <> 'EN MANTENIMIENTO' " +
            "OR a.estadoDeMantenimiento IS NULL")
    List<Avion> findAllOptimized();

    Optional<Avion> findByNumeroDeRegistro(String numeroDeRegistro);

    boolean existsByNumeroDeRegistro(String numeroDeRegistro);

    @Query("SELECT DISTINCT a FROM Avion a LEFT JOIN FETCH a.modeloAvion m LEFT JOIN FETCH m.fabricante LEFT JOIN FETCH a.capacidadClases1 c LEFT JOIN FETCH c.claseAsiento1 ORDER BY a.idAvion DESC")
    List<Avion> findAllConRelaciones();


    @Query("SELECT DISTINCT a FROM Avion a " +
            "LEFT JOIN FETCH a.modeloAvion m " +
            "WHERE (UPPER(a.estadoDeMantenimiento) <> 'EN MANTENIMIENTO' OR a.estadoDeMantenimiento IS NULL) " +
            "AND a.idAvion NOT IN (" +
            "   SELECT DISTINCT v.avion.idAvion FROM Vuelo v " +
            "   WHERE v.avion IS NOT NULL " +
            "   AND (:idVueloActual IS NULL OR v.idVuelo <> :idVueloActual) " +
            "   AND v.fechaHoraSalida < :fechaLlegada " +
            "   AND v.fechaHoraLlegada > :fechaSalida" +
            ")")
    List<Avion> findAvionesDisponiblesEnRango(
            @Param("fechaSalida") Timestamp fechaSalida,
            @Param("fechaLlegada") Timestamp fechaLlegada,
            @Param("idVueloActual") Integer idVueloActual
    );
}