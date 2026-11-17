package com.SkyWay.modules.asignacionpuerta.domain.repository;


import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface AsignacionPuertaRepository extends JpaRepository<AsignacionPuerta, Integer> {
    // Puedes agregar métodos personalizados aquí si lo necesitas
    List<AsignacionPuerta> findBySegmentoVuelo(SegmentoVuelo segmentoVuelo);

    /**
     * Detecta asignaciones activas de la misma puerta en otros vuelos dentro de la ventana de embarque.
     */
    @Query("SELECT ap FROM AsignacionPuerta ap " +
           "JOIN ap.segmentoVuelo s " +
           "JOIN s.vuelo v " +
           "LEFT JOIN v.estadoVuelo ev " +
           "WHERE ap.puertaEmbarque.idPuerta = :idPuerta " +
           "AND v.idVuelo <> :idVuelo " +
           "AND (ev IS NULL OR (" +
           "    UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%' " +
           "    AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%' " +
           "    AND UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO'" +
           ")) " +
           "AND s.horaSalida < :ventanaSalidaFin " +
           "AND s.horaSalida > :ventanaSalidaInicio")
    List<AsignacionPuerta> findConflictosPuertaEnVentana(
            @Param("idPuerta") Integer idPuerta,
            @Param("idVuelo") Integer idVuelo,
            @Param("ventanaSalidaInicio") Timestamp ventanaSalidaInicio,
            @Param("ventanaSalidaFin") Timestamp ventanaSalidaFin
    );
}
