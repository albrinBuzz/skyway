package com.SkyWay.modules.segmentovuelo.domain.repository;




import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.presentation.dto.PuntoRutaProjection;
import com.SkyWay.modules.segmentovuelo.presentation.dto.SegmentoMapaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentoVueloRepository extends JpaRepository<SegmentoVuelo, Integer> {
    // Métodos CRUD ya están incluidos por JpaRepository

    @Query(value = "SELECT id_segmento, hora_llegada - hora_salida AS duracion FROM segmento_vuelo", nativeQuery = true)
    List<Object[]> obtenerDuraciones();

    @Query("SELECT s FROM SegmentoVuelo s WHERE s.vuelo.idVuelo = :idVuelo ORDER BY s.ordenSegmento")
    List<SegmentoVuelo> buscarPorVuelo(@Param("idVuelo") Integer idVuelo);


    @Query(value = """
        SELECT
            ae.id_aeropuerto        AS idAeropuerto,
            ae.codigo_iata          AS codigoIata,
            ae.nombre_aeropuerto    AS nombreAeropuerto,
            c.nombre                AS ciudad,
            ST_Y(ae.posicion::geometry) AS latitud,
            ST_X(ae.posicion::geometry) AS longitud,
            sub.orden_global         AS ordenGlobal,
            sub.rol                  AS rol
        FROM (
            SELECT sv.id_aeropuerto_origen AS id_aeropuerto,
                   (iv.orden * 1000 + sv.orden_segmento * 2)     AS orden_global,
                   'ORIGEN' AS rol
            FROM itinerario_vuelo iv
            JOIN segmento_vuelo sv ON sv.id_vuelo = iv.id_vuelo
            WHERE iv.id_itinerario = :idItinerario

            UNION ALL

            SELECT sv.id_aeropuerto_destino AS id_aeropuerto,
                   (iv.orden * 1000 + sv.orden_segmento * 2 + 1) AS orden_global,
                   'DESTINO' AS rol
            FROM itinerario_vuelo iv
            JOIN segmento_vuelo sv ON sv.id_vuelo = iv.id_vuelo
            WHERE iv.id_itinerario = :idItinerario
        ) sub
        JOIN aeropuerto ae ON ae.id_aeropuerto = sub.id_aeropuerto
        JOIN ciudad c ON c.id_ciudad = ae.id_ciudad
        ORDER BY sub.orden_global
        """, nativeQuery = true)
    List<PuntoRutaProjection> obtenerRutaPorItinerario(@Param("idItinerario") Integer idItinerario);




    @Query(value = """
        SELECT
            sv.id_segmento          AS idSegmento,
            sv.id_vuelo             AS idVuelo,
            sv.orden_segmento       AS ordenSegmento,
            ao.codigo_iata          AS iataOrigen,
            ao.nombre_aeropuerto    AS nombreOrigen,
            ST_Y(ao.posicion::geometry) AS latOrigen,
            ST_X(ao.posicion::geometry) AS lngOrigen,
            ad.codigo_iata          AS iataDestino,
            ad.nombre_aeropuerto    AS nombreDestino,
            ST_Y(ad.posicion::geometry) AS latDestino,
            ST_X(ad.posicion::geometry) AS lngDestino
        FROM segmento_vuelo sv
        JOIN aeropuerto ao ON ao.id_aeropuerto = sv.id_aeropuerto_origen
        JOIN aeropuerto ad ON ad.id_aeropuerto = sv.id_aeropuerto_destino
        WHERE sv.id_vuelo = :idVuelo
        ORDER BY sv.orden_segmento
        """, nativeQuery = true)
    List<SegmentoMapaProjection> findGeometriaPorVuelo(@Param("idVuelo") Integer idVuelo);


}
