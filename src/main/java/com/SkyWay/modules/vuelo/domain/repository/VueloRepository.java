package com.SkyWay.modules.vuelo.domain.repository;

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
        SELECT DISTINCT
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
        """, nativeQuery = true)
	List<VueloEstadoProjection> buscarEstadoVuelosVivo(
			@Param("numeroVuelo") String numeroVuelo,
			@Param("ruta") String ruta
	);
	
}
