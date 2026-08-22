package com.SkyWay.modules.itinerario.domain.repository;


import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioResumenDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface ItinerarioRepository extends JpaRepository<Itinerario, Integer> {

    // Ejecutar la función fnBuscarVuelo en PostgreSQL y mapear el resultado al DTO
    @Query(value = "SELECT * FROM fnBuscarVuelo(:p_ciudad_salida, :p_ciudad_llegada, :p_fecha_inicio)", nativeQuery = true)
    List<ItinerarioDTO> buscarItinerariosPorCiudadesYFecha(
            @Param("p_ciudad_salida") String ciudadSalida,
            @Param("p_ciudad_llegada") String ciudadLlegada,
            @Param("p_fecha_inicio") String fechaInicio
    );

    @Query(value = "SELECT * FROM fnBuscarVuelo(:p_ciudad_salida, :p_ciudad_llegada, :p_fecha_inicio)", nativeQuery = true)
    List<ItinerarioDTO> buscarItinerariosPorCiudadesYFechaPostgres(
            @Param("p_ciudad_salida") String ciudadSalida,
            @Param("p_ciudad_llegada") String ciudadLlegada,
            @Param("p_fecha_inicio") Date fechaInicio
    );

    @Query("SELECT DISTINCT i FROM Itinerario i " +
            "LEFT JOIN FETCH i.aeropuertoOrigen " +
            "LEFT JOIN FETCH i.aeropuertoDestino")
    List<Itinerario> findAllOptimized();

    @Query(value = """
        SELECT i.*
        FROM reserva r
        JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
        JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
        WHERE r.rut_pasajero = :rut
        ORDER BY r.fecha_reserva ASC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM reserva r
        JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
        JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
        WHERE r.rut_pasajero = :rut
        """,
            nativeQuery = true)
    Page<Itinerario> findItinerariosByRut(@Param("rut") String rut, Pageable pageable);

    @Query(value = """
        SELECT 
            i.id_itinerario AS idItinerario,
            arp2.codigo_iata AS codigoIataOrigen,
            arp1.codigo_iata AS codigoIataDestino,
            i.hora_salida AS horaSalida
        FROM reserva r
        JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
        JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
        JOIN aeropuerto arp1 ON arp1.id_aeropuerto = i.destino_aeropuerto
        JOIN aeropuerto arp2 ON arp2.id_aeropuerto = i.origen_aeropuerto
        WHERE r.rut_pasajero = :rut
        ORDER BY r.fecha_reserva ASC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ItinerarioResumenDTO> findResumenByRut(
            @Param("rut") String rut,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = "SELECT COUNT(*) FROM reserva r WHERE r.rut_pasajero = :rut", nativeQuery = true)
    int countByRut(@Param("rut") String rut);


    @Query(value = """
    SELECT 
        i.id_itinerario AS idItinerario,
        arp2.codigo_iata AS codigoIataOrigen,
        arp1.codigo_iata AS codigoIataDestino,
        i.hora_salida AS horaSalida
    FROM reserva r
    JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
    JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
    JOIN aeropuerto arp1 ON arp1.id_aeropuerto = i.destino_aeropuerto
    JOIN aeropuerto arp2 ON arp2.id_aeropuerto = i.origen_aeropuerto
    WHERE r.rut_pasajero = :rut
      AND (:fechaInicio IS NULL OR i.hora_salida >= :fechaInicio)
      AND (:fechaFin IS NULL OR i.hora_salida <= :fechaFin)
    ORDER BY r.fecha_reserva ASC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<ItinerarioResumenDTO> buscarConFiltroFechas(
            @Param("rut") String rut,
            @Param("fechaInicio") Timestamp fechaInicio,
            @Param("fechaFin") Timestamp fechaFin,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

}
