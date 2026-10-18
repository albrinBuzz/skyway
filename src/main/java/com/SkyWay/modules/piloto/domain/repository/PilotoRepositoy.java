package com.SkyWay.modules.piloto.domain.repository;

import com.SkyWay.modules.piloto.domain.model.Piloto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface PilotoRepositoy extends JpaRepository<Piloto, String>{

    @Query("select p FROM Piloto p where p.rut = ?1")
    Optional<Piloto> findByCorreoElectronico(String correoElectronico);


    @Query("SELECT p FROM Piloto p " +
            "WHERE p.rut NOT IN (" +
            "   SELECT DISTINCT v.piloto.rut FROM Vuelo v " +
            "   WHERE v.piloto IS NOT NULL " +
            "   AND (:idVueloActual IS NULL OR v.idVuelo <> :idVueloActual) " +
            "   AND v.fechaHoraSalida < :fechaLlegada " +
            "   AND v.fechaHoraLlegada > :fechaSalida" +
            ")")
    List<Piloto> findPilotosDisponiblesEnRango(
            @Param("fechaSalida") Timestamp fechaSalida,
            @Param("fechaLlegada") Timestamp fechaLlegada,
            @Param("idVueloActual") Integer idVueloActual
    );
}
