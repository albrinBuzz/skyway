package com.SkyWay.modules.aeropuerto.domain.repository;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AeropuertoRepository extends JpaRepository<Aeropuerto, Integer>{
    @Query(value = """
        SELECT
            a.id_aeropuerto     AS idAeropuerto,
            a.codigo_iata       AS codigoIata,
            a.nombre_aeropuerto AS nombreAeropuerto,
            c.nombre            AS ciudad,
            ST_Y(a.posicion::geometry) AS latitud,
            ST_X(a.posicion::geometry) AS longitud
        FROM aeropuerto a
        JOIN ciudad c ON c.id_ciudad = a.id_ciudad
        ORDER BY a.nombre_aeropuerto
        """, nativeQuery = true)
    List<AeropuertoMapaProjection> findAllConCoordenadas();
}
