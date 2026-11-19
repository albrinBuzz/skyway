package com.SkyWay.modules.vuelo.domain.repository;

import java.sql.Timestamp;
import java.util.List;

import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




import jakarta.enterprise.context.ApplicationScoped;

import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.presentation.dto.VueloEstadoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
@ApplicationScoped
public interface VueloRepository extends JpaRepository<Vuelo, Integer>{

	List<Vuelo> findByPiloto(Piloto piloto);


	@Query(value = """
        SELECT
            v.id_vuelo AS idVuelo,
            v.numero_vuelo AS numeroVuelo,
            c_orig.nombre AS origenCiudad,
            a_orig.codigo_iata AS origenIata,
            c_dest.nombre AS destinoCiudad,
            a_dest.codigo_iata AS destinoIata,
            v.fecha_hora_salida AS fechaHoraSalida,
            ev.descripcion AS estadoNombre,
            pe.codigo_puerta AS codigoPuerta
        FROM vuelo v
        JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
        LEFT JOIN segmento_vuelo s_orig ON s_orig.id_vuelo = v.id_vuelo 
             AND s_orig.orden_segmento = (
                 SELECT MIN(s_sub.orden_segmento) 
                 FROM segmento_vuelo s_sub 
                 WHERE s_sub.id_vuelo = v.id_vuelo
             )
        LEFT JOIN aeropuerto a_orig ON a_orig.id_aeropuerto = s_orig.id_aeropuerto_origen
        LEFT JOIN ciudad c_orig ON c_orig.id_ciudad = a_orig.id_ciudad
        
        LEFT JOIN segmento_vuelo s_dest ON s_dest.id_vuelo = v.id_vuelo 
             AND s_dest.orden_segmento = (
                 SELECT MAX(s_sub2.orden_segmento) 
                 FROM segmento_vuelo s_sub2 
                 WHERE s_sub2.id_vuelo = v.id_vuelo
             )
        LEFT JOIN aeropuerto a_dest ON a_dest.id_aeropuerto = s_dest.id_aeropuerto_destino
        LEFT JOIN ciudad c_dest ON c_dest.id_ciudad = a_dest.id_ciudad
        
        LEFT JOIN asignacion_puerta ap ON ap.id_segmento = s_orig.id_segmento
        LEFT JOIN puerta_embarque pe ON pe.id_puerta = ap.id_puerta
        
        WHERE (:numeroVuelo IS NULL OR LOWER(v.numero_vuelo) LIKE LOWER(CONCAT('%', :numeroVuelo, '%')))
          AND (:ruta IS NULL OR LOWER(c_orig.nombre) LIKE LOWER(CONCAT('%', :ruta, '%'))
                             OR LOWER(c_dest.nombre) LIKE LOWER(CONCAT('%', :ruta, '%'))
                             OR LOWER(a_orig.codigo_iata) LIKE LOWER(CONCAT('%', :ruta, '%'))
                             OR LOWER(a_dest.codigo_iata) LIKE LOWER(CONCAT('%', :ruta, '%')))
        ORDER BY v.fecha_hora_salida ASC
        LIMIT 100
        """, nativeQuery = true)
	List<VueloEstadoProjection> buscarEstadoVuelosVivo(
			@Param("numeroVuelo") String numeroVuelo,
			@Param("ruta") String ruta
	);



	@Query("SELECT COUNT(v) > 0 FROM Vuelo v " +
			"LEFT JOIN v.estadoVuelo ev " +
			"WHERE v.piloto.rut = :rutPiloto " +
			"AND (:idVueloActual IS NULL OR v.idVuelo <> :idVueloActual) " +
			"AND (ev IS NULL OR (" +
			"    UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%' " +
			"    AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%' " +
			"    AND UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO'" +
			")) " +
			"AND v.fechaHoraSalida < :fechaLlegada " +
			"AND v.fechaHoraLlegada > :fechaSalida")
	boolean existeConflictoPiloto(
			@Param("rutPiloto") String rutPiloto,
			@Param("fechaSalida") Timestamp fechaSalida,
			@Param("fechaLlegada") Timestamp fechaLlegada,
			@Param("idVueloActual") Integer idVueloActual
	);

	// Comprueba si la aeronave está asignada a otro vuelo activo que se solape en fechas
	@Query("SELECT COUNT(v) > 0 FROM Vuelo v " +
			"LEFT JOIN v.estadoVuelo ev " +
			"WHERE v.avion.idAvion = :idAvion " +
			"AND (:idVueloActual IS NULL OR v.idVuelo <> :idVueloActual) " +
			"AND (ev IS NULL OR (" +
			"    UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%' " +
			"    AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%' " +
			"    AND UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO'" +
			")) " +
			"AND v.fechaHoraSalida < :fechaLlegada " +
			"AND v.fechaHoraLlegada > :fechaSalida")
	boolean existeConflictoAvion(
			@Param("idAvion") Integer idAvion,
			@Param("fechaSalida") Timestamp fechaSalida,
			@Param("fechaLlegada") Timestamp fechaLlegada,
			@Param("idVueloActual") Integer idVueloActual
	);

	@Query(value = """
		WITH vuelos_top AS (
			SELECT
				v.id_vuelo,
				v.numero_vuelo,
				v.fecha_hora_salida,
				v.fecha_hora_llegada,
				v.id_avion,
				COALESCE(ev.descripcion, 'Programado') AS estado
			FROM vuelo v
			LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
			WHERE (ev.id_estado_vuelo IS NULL OR UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%')
			  AND v.fecha_hora_salida >= NOW()
			ORDER BY v.fecha_hora_salida ASC
			LIMIT :limite
		)
		SELECT
			vt.numero_vuelo,
			ao.codigo_iata AS orig_iata,
			co.nombre AS orig_ciudad,
			ad.codigo_iata AS dest_iata,
			cd.nombre AS dest_ciudad,
			vt.fecha_hora_salida,
			vt.fecha_hora_llegada,
			COALESCE(m.nombre, 'Aeronave asignada') AS modelo,
			vt.estado
		FROM vuelos_top vt
		LEFT JOIN avion av ON av.id_avion = vt.id_avion
		LEFT JOIN modelo_avion m ON m.id_modelo = av.id_modelo
		JOIN segmento_vuelo s1 ON s1.id_vuelo = vt.id_vuelo
			 AND s1.orden_segmento = (SELECT MIN(sx.orden_segmento) FROM segmento_vuelo sx WHERE sx.id_vuelo = vt.id_vuelo)
		JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen
		JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
		JOIN segmento_vuelo s2 ON s2.id_vuelo = vt.id_vuelo
			 AND s2.orden_segmento = (SELECT MAX(sy.orden_segmento) FROM segmento_vuelo sy WHERE sy.id_vuelo = vt.id_vuelo)
		JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino
		JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
		ORDER BY vt.fecha_hora_salida ASC
		""", nativeQuery = true)
	List<Object[]> findProximosVuelosVigentesPortal(@Param("limite") int limite);

	@Query(value = """
		WITH vuelos_top AS (
			SELECT
				v.id_vuelo,
				v.numero_vuelo,
				v.fecha_hora_salida,
				v.fecha_hora_llegada,
				v.id_avion,
				COALESCE(ev.descripcion, 'Programado') AS estado
			FROM vuelo v
			LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
			WHERE (ev.id_estado_vuelo IS NULL OR UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%')
			ORDER BY v.fecha_hora_salida DESC
			LIMIT :limite
		)
		SELECT
			vt.numero_vuelo,
			ao.codigo_iata AS orig_iata,
			co.nombre AS orig_ciudad,
			ad.codigo_iata AS dest_iata,
			cd.nombre AS dest_ciudad,
			vt.fecha_hora_salida,
			vt.fecha_hora_llegada,
			COALESCE(m.nombre, 'Aeronave asignada') AS modelo,
			vt.estado
		FROM vuelos_top vt
		LEFT JOIN avion av ON av.id_avion = vt.id_avion
		LEFT JOIN modelo_avion m ON m.id_modelo = av.id_modelo
		JOIN segmento_vuelo s1 ON s1.id_vuelo = vt.id_vuelo
			 AND s1.orden_segmento = (SELECT MIN(sx.orden_segmento) FROM segmento_vuelo sx WHERE sx.id_vuelo = vt.id_vuelo)
		JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen
		JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
		JOIN segmento_vuelo s2 ON s2.id_vuelo = vt.id_vuelo
			 AND s2.orden_segmento = (SELECT MAX(sy.orden_segmento) FROM segmento_vuelo sy WHERE sy.id_vuelo = vt.id_vuelo)
		JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino
		JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
		ORDER BY vt.fecha_hora_salida DESC
		""", nativeQuery = true)
	List<Object[]> findUltimosVuelosPortal(@Param("limite") int limite);

}
