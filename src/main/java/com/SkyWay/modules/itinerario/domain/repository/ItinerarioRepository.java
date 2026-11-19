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

    @Query(value = "SELECT GREATEST(0, COALESCE(reltuples::bigint, 0)) FROM pg_class WHERE relname = 'itinerario'", nativeQuery = true)
    Long obtenerEstimacionReltuples();

    @Query(value = """
        WITH muestra AS (
            SELECT i.origen_aeropuerto, i.destino_aeropuerto, i.precio_base, i.numero_escalas, i.hora_salida
            FROM itinerario i
            WHERE i.hora_salida >= NOW()
              AND NOT EXISTS (
                  SELECT 1
                  FROM itinerario_vuelo iv_chk
                  JOIN vuelo v_chk ON v_chk.id_vuelo = iv_chk.id_vuelo
                  JOIN estado_vuelo ev_chk ON ev_chk.id_estado_vuelo = v_chk.id_estado_vuelo
                  WHERE iv_chk.id_itinerario = i.id_itinerario
                    AND (
                        UPPER(COALESCE(ev_chk.descripcion, '')) LIKE '%CANCEL%'
                        OR UPPER(COALESCE(ev_chk.estado, '')) LIKE '%CANCEL%'
                        OR UPPER(COALESCE(ev_chk.estado, '')) = 'INACTIVO'
                    )
              )
            ORDER BY i.hora_salida ASC
            LIMIT 1200
        ),
        agrupado_dia AS (
            SELECT
                ao.codigo_iata AS orig_iata,
                ao.nombre_aeropuerto AS orig_nombre,
                co.nombre AS orig_ciudad,
                po.nombre AS orig_pais,
                ad.codigo_iata AS dest_iata,
                ad.nombre_aeropuerto AS dest_nombre,
                cd.nombre AS dest_ciudad,
                pd.nombre AS dest_pais,
                MIN(m.precio_base) AS precio_min,
                MIN(m.numero_escalas) AS escalas_min,
                COUNT(*) AS opciones,
                DATE(m.hora_salida) AS fecha_dia,
                ROW_NUMBER() OVER (
                    PARTITION BY ao.codigo_iata, ad.codigo_iata
                    ORDER BY COUNT(*) DESC, MIN(m.precio_base) ASC, DATE(m.hora_salida) ASC
                ) AS rn
            FROM muestra m
            JOIN aeropuerto ao ON ao.id_aeropuerto = m.origen_aeropuerto
            JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
            JOIN pais po ON po.id_pais = co.id_pais
            JOIN aeropuerto ad ON ad.id_aeropuerto = m.destino_aeropuerto
            JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
            JOIN pais pd ON pd.id_pais = cd.id_pais
            WHERE ao.activo = true AND ad.activo = true
            GROUP BY ao.codigo_iata, ao.nombre_aeropuerto, co.nombre, po.nombre,
                     ad.codigo_iata, ad.nombre_aeropuerto, cd.nombre, pd.nombre,
                     DATE(m.hora_salida)
        )
        SELECT
            orig_iata, orig_nombre, orig_ciudad, orig_pais,
            dest_iata, dest_nombre, dest_ciudad, dest_pais,
            precio_min, escalas_min, opciones,
            TO_CHAR(fecha_dia, 'YYYY-MM-DD') AS fecha_url,
            TO_CHAR(fecha_dia, 'DD/MM/YYYY') AS fecha_label
        FROM agrupado_dia
        WHERE rn = 1
        ORDER BY opciones DESC, precio_min ASC, fecha_dia ASC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findRutasDestacadasVigentesPortal(@Param("limite") int limite);

    @Query(value = """
        WITH muestra AS (
            SELECT i.origen_aeropuerto, i.destino_aeropuerto, i.precio_base, i.numero_escalas, i.hora_salida
            FROM itinerario i
            WHERE NOT EXISTS (
                  SELECT 1
                  FROM itinerario_vuelo iv_chk
                  JOIN vuelo v_chk ON v_chk.id_vuelo = iv_chk.id_vuelo
                  JOIN estado_vuelo ev_chk ON ev_chk.id_estado_vuelo = v_chk.id_estado_vuelo
                  WHERE iv_chk.id_itinerario = i.id_itinerario
                    AND (
                        UPPER(COALESCE(ev_chk.descripcion, '')) LIKE '%CANCEL%'
                        OR UPPER(COALESCE(ev_chk.estado, '')) LIKE '%CANCEL%'
                        OR UPPER(COALESCE(ev_chk.estado, '')) = 'INACTIVO'
                    )
              )
            ORDER BY i.hora_salida DESC
            LIMIT 1200
        ),
        agrupado_dia AS (
            SELECT
                ao.codigo_iata AS orig_iata,
                ao.nombre_aeropuerto AS orig_nombre,
                co.nombre AS orig_ciudad,
                po.nombre AS orig_pais,
                ad.codigo_iata AS dest_iata,
                ad.nombre_aeropuerto AS dest_nombre,
                cd.nombre AS dest_ciudad,
                pd.nombre AS dest_pais,
                MIN(m.precio_base) AS precio_min,
                MIN(m.numero_escalas) AS escalas_min,
                COUNT(*) AS opciones,
                DATE(m.hora_salida) AS fecha_dia,
                ROW_NUMBER() OVER (
                    PARTITION BY ao.codigo_iata, ad.codigo_iata
                    ORDER BY COUNT(*) DESC, MIN(m.precio_base) ASC, DATE(m.hora_salida) DESC
                ) AS rn
            FROM muestra m
            JOIN aeropuerto ao ON ao.id_aeropuerto = m.origen_aeropuerto
            JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
            JOIN pais po ON po.id_pais = co.id_pais
            JOIN aeropuerto ad ON ad.id_aeropuerto = m.destino_aeropuerto
            JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
            JOIN pais pd ON pd.id_pais = cd.id_pais
            WHERE ao.activo = true AND ad.activo = true
            GROUP BY ao.codigo_iata, ao.nombre_aeropuerto, co.nombre, po.nombre,
                     ad.codigo_iata, ad.nombre_aeropuerto, cd.nombre, pd.nombre,
                     DATE(m.hora_salida)
        )
        SELECT
            orig_iata, orig_nombre, orig_ciudad, orig_pais,
            dest_iata, dest_nombre, dest_ciudad, dest_pais,
            precio_min, escalas_min, opciones,
            TO_CHAR(fecha_dia, 'YYYY-MM-DD') AS fecha_url,
            TO_CHAR(fecha_dia, 'DD/MM/YYYY') AS fecha_label
        FROM agrupado_dia
        WHERE rn = 1
        ORDER BY opciones DESC, precio_min ASC, fecha_dia DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findRutasDestacadasRecientesPortal(@Param("limite") int limite);

}
