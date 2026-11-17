package com.SkyWay.modules.turnotripulacion.domain.repository;


import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoTripulacionRepository extends JpaRepository<TurnoTripulacion, Integer> {

    Optional<TurnoTripulacion> findByIdTurnoTripulacion(Integer idTurnoTripulacion);

    // Find TurnoTripulacion by Tripulacion's rut
    List<TurnoTripulacion> findByTripulacion1Rut(String rut);

    // Find TurnoTripulacion by Turno's id
    List<TurnoTripulacion> findByTurno1IdTurno(Integer idTurno);

    /**
     * Detecta tripulantes asignados al vuelo actual cuyo turno se solapa con otro vuelo activo en el nuevo rango horario.
     */
    @Query("SELECT ttOther FROM TurnoTripulacion ttCurrent " +
           "JOIN ttCurrent.turno1 tCurrent " +
           "JOIN ttCurrent.tripulacion1 trip " +
           "JOIN TurnoTripulacion ttOther ON ttOther.tripulacion1.rut = trip.rut " +
           "JOIN ttOther.turno1 tOther " +
           "JOIN tOther.vuelo vOther " +
           "LEFT JOIN vOther.estadoVuelo evOther " +
           "WHERE tCurrent.vuelo.idVuelo = :idVuelo " +
           "AND vOther.idVuelo <> :idVuelo " +
           "AND UPPER(COALESCE(ttCurrent.estado, 'PENDIENTE')) NOT IN ('RECHAZADO', 'CANCELADO') " +
           "AND UPPER(COALESCE(ttOther.estado, 'PENDIENTE')) NOT IN ('RECHAZADO', 'CANCELADO') " +
           "AND (evOther IS NULL OR (" +
           "    UPPER(COALESCE(evOther.descripcion, '')) NOT LIKE '%CANCEL%' " +
           "    AND UPPER(COALESCE(evOther.estado, '')) NOT LIKE '%CANCEL%' " +
           "    AND UPPER(COALESCE(evOther.estado, '')) <> 'INACTIVO'" +
           ")) " +
           "AND tOther.horaInicio < :nuevaLlegada " +
           "AND tOther.horaFin > :nuevaSalida")
    List<TurnoTripulacion> findConflictosTripulacionEnRango(
            @Param("idVuelo") Integer idVuelo,
            @Param("nuevaSalida") Timestamp nuevaSalida,
            @Param("nuevaLlegada") Timestamp nuevaLlegada
    );
}
